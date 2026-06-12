package com.nonsense.chat.ui.common;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Tiny async image loader (avatars + inline photos). No third-party library: OkHttp fetch +
 * in-memory {@link LruCache}, decoded off the main thread and posted back. The target ImageView is
 * tagged with the URL so recycled rows in a RecyclerView never show the wrong picture.
 *
 * HTTP/1.1 is pinned for the same reason {@code Http.java} does it — HTTP/2 stalls behind Cloudflare
 * on some devices.
 *
 * Uses its OWN OkHttpClient (dedicated pool + dispatcher), NOT the shared REST client: the REST/WS
 * client kept long-lived connections that Cloudflare closes idle, and reusing such a stale connection
 * made the body read hang until timeout (SocketTimeoutException / "Socket closed"). A short keep-alive
 * + per-fetch retry on timeout fixes that.
 */
@Singleton
public final class ImageLoader {

    private static final String TAG = "ImageLoader";
    private static final int TAG_KEY = 0x7f5e0001; // arbitrary, stable per-process
    private static final int MAX_ATTEMPTS = 3;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .callTimeout(90, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            // KEEP-ALIVE pool: REST/WS work because they ride a warm connection; opening a brand-new
            // TLS connection per image was timing out on this device, so we reuse a warm one too. The
            // explicit retry below re-issues the call (fresh route) if a pooled connection went stale.
            .connectionPool(new ConnectionPool(5, 5, TimeUnit.MINUTES))
            .dispatcher(new Dispatcher()) // own dispatcher — never queue behind REST/WS calls
            .protocols(Arrays.asList(Protocol.HTTP_1_1))
            .build();

    // Only 2 concurrent downloads: opening 3 fresh TLS connections at once was timing out on-device.
    private final ExecutorService io = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "image-loader");
        t.setDaemon(true);
        return t;
    });

    private final Handler ui = new Handler(Looper.getMainLooper());

    private final LruCache<String, Bitmap> cache;

    @Inject
    public ImageLoader() {
        int maxKb = (int) (Runtime.getRuntime().maxMemory() / 1024);
        cache = new LruCache<String, Bitmap>(maxKb / 8) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount() / 1024;
            }
        };
    }

    public void load(@NonNull ImageView iv, String url) {
        load(iv, url, false);
    }

    public void loadCircle(@NonNull ImageView iv, String url) {
        load(iv, url, true);
    }

    private void load(@NonNull ImageView iv, String url, boolean circle) {
        if (!isHttpUrl(url)) {
            // ignore empty / emoji / text avatars — never feed OkHttp junk
            if (url != null && !url.isEmpty()) Log.d(TAG, "skip non-http value: " + url);
            return;
        }
        String key = (circle ? "c:" : "r:") + url;
        iv.setTag(TAG_KEY, key);

        Bitmap cached = cache.get(key);
        if (cached != null) {
            iv.setImageBitmap(cached);
            return;
        }
        io.execute(() -> {
            Bitmap bmp = fetch(url);
            if (bmp == null) { Log.w(TAG, "no bitmap for " + url); return; }
            final Bitmap out = circle ? circleCrop(bmp) : bmp;
            cache.put(key, out);
            ui.post(() -> {
                Object tag = iv.getTag(TAG_KEY);
                if (key.equals(tag)) iv.setImageBitmap(out);
            });
        });
    }

    // Max edge for proxy/downsample.
    private static final int MAX_EDGE = 1280;

    /**
     * Builds the list of URLs to try, in order. On some networks (e.g. certain ISPs / regions) the
     * direct Supabase Storage download stalls even though REST works — so we first try a third-party
     * image CDN that fetches the file server-side and re-serves it (smaller WebP) over a different
     * network path, then fall back to the original direct URL.
     */
    private List<String> candidates(String url) {
        List<String> list = new ArrayList<>(2);
        String proxied = proxied(url);
        if (proxied != null) list.add(proxied);
        list.add(url);
        return list;
    }

    private String proxied(String url) {
        // Only proxy public Supabase Storage objects; leave anything else direct.
        if (url == null || !url.contains("/storage/v1/object/public/")) return null;
        // weserv expects the source without the scheme.
        String src = url.replaceFirst("^https?://", "");
        try {
            return "https://images.weserv.nl/?url=" + URLEncoder.encode(src, "UTF-8")
                    + "&w=" + MAX_EDGE + "&output=webp&n=-1";
        } catch (UnsupportedEncodingException e) {
            return null; // UTF-8 always present; never happens
        }
    }

    private Bitmap fetch(String url) {
        for (String candidate : candidates(url)) {
            Bitmap b = fetchOne(candidate, url);
            if (b != null) return b;
            Log.w(TAG, "candidate failed, trying next for " + url);
        }
        return null;
    }

    private Bitmap fetchOne(String fetchUrl, String url) {
        Request req = new Request.Builder().url(fetchUrl).get().build();
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try (Response resp = client.newCall(req).execute()) {
                if (!resp.isSuccessful()) {
                    Log.w(TAG, "HTTP " + resp.code() + " for " + fetchUrl);
                    return null; // a real HTTP error won't be fixed by retrying
                }
                ResponseBody body = resp.body();
                if (body == null) return null;
                byte[] bytes = body.bytes();
                return decode(bytes, url);
            } catch (java.io.IOException e) {
                // Timeouts / stale-connection / transient network errors: retry with a new connection.
                Log.w(TAG, "fetch attempt " + attempt + "/" + MAX_ATTEMPTS + " failed for " + fetchUrl + ": " + e);
                if (attempt == MAX_ATTEMPTS) return null;
                // Drop any pooled (possibly half-dead) connections, then back off briefly before retry.
                client.connectionPool().evictAll();
                try { Thread.sleep(400L * attempt); } catch (InterruptedException ie) { return null; }
            } catch (Throwable t) {
                // OOM / decode failure / anything else — never crash the background thread, don't retry.
                Log.w(TAG, "fetch failed for " + fetchUrl + ": " + t, t);
                return null;
            }
        }
        return null;
    }

    private static Bitmap decode(byte[] bytes, String url) {
        // Two-pass decode with downsampling (like Coil): full-res decoding of a multi-MB photo can
        // OOM on a phone and silently yield no image. Cap the longest side ~1280px.
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bytes, 0, bytes.length, bounds);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight, 1280);
        // keep ARGB_8888 (default) so transparent stickers / circular avatars stay clean
        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
        if (bmp == null) Log.w(TAG, "decode failed (" + bytes.length + "B) for " + url);
        return bmp;
    }

    public static boolean isHttpUrl(String url) {
        return url != null && (url.startsWith("http://") || url.startsWith("https://"));
    }

    /** Largest power-of-two sample size that keeps the longest side >= maxSide. */
    private static int sampleSize(int w, int h, int maxSide) {
        int sample = 1;
        int longest = Math.max(w, h);
        if (longest <= 0) return 1;
        while (longest / sample > maxSide) sample *= 2;
        return sample;
    }

    private static Bitmap circleCrop(Bitmap src) {
        int size = Math.min(src.getWidth(), src.getHeight());
        int x = (src.getWidth() - size) / 2;
        int y = (src.getHeight() - size) / 2;
        Bitmap squared = Bitmap.createBitmap(src, x, y, size, size);
        Bitmap out = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(out);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setShader(new BitmapShader(squared, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
        float r = size / 2f;
        canvas.drawCircle(r, r, r, paint);
        return out;
    }
}
