package com.nonsense.chat.ui.common;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.widget.ImageView;

import androidx.annotation.NonNull;

/**
 * Telegram-style circular avatars. When the user/chat has no uploaded picture we draw a coloured
 * circle with the initials; the colour is derived deterministically from a key (uid) so the same
 * person always gets the same hue. If an avatar URL exists, {@link ImageLoader} replaces it.
 */
public final class Avatars {

    private Avatars() {}

    private static final int[] PALETTE = {
            0xFFE17076, 0xFFF8A14E, 0xFFE5CA77, 0xFF7BC862,
            0xFF6EC9CB, 0xFF65AADD, 0xFF8B6FC4, 0xFFEE7AAE
    };

    /**
     * Set a circular avatar on the ImageView. Shows initials immediately; if {@code avatarUrl} is
     * non-empty, asynchronously loads and circle-crops the real image over the top.
     */
    public static void set(@NonNull ImageView iv, String name, String key,
                           String avatarUrl, ImageLoader loader) {
        iv.setImageDrawable(new InitialsDrawable(initials(name), color(key)));
        // Only try to fetch real picture URLs. Many avatar fields hold an emoji or plain text
        // (e.g. "⏳ Загрузка…") — those must stay as the initials drawable, never hit the network.
        if (loader != null && ImageLoader.isHttpUrl(avatarUrl)) {
            loader.loadCircle(iv, avatarUrl);
        }
    }

    public static String initials(String name) {
        if (TextUtils.isEmpty(name)) return "?";
        String trimmed = name.trim();
        String[] parts = trimmed.split("\\s+");
        StringBuilder sb = new StringBuilder();
        sb.append(Character.toUpperCase(parts[0].charAt(0)));
        if (parts.length > 1 && !parts[1].isEmpty()) {
            sb.append(Character.toUpperCase(parts[1].charAt(0)));
        }
        return sb.toString();
    }

    public static int color(String key) {
        int h = 0;
        if (key != null) {
            for (int i = 0; i < key.length(); i++) h = key.charAt(i) + ((h << 5) - h);
        }
        return PALETTE[Math.abs(h) % PALETTE.length];
    }

    /** A flat coloured circle with centered white initials. */
    public static final class InitialsDrawable extends Drawable {
        private final Paint circle = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final String text;
        private final Rect bounds = new Rect();

        public InitialsDrawable(String text, int color) {
            this.text = text == null ? "?" : text;
            circle.setColor(color);
            textPaint.setColor(Color.WHITE);
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setFakeBoldText(true);
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            Rect b = getBounds();
            float cx = b.exactCenterX();
            float cy = b.exactCenterY();
            float r = Math.min(b.width(), b.height()) / 2f;
            canvas.drawCircle(cx, cy, r, circle);
            textPaint.setTextSize(r * 0.9f);
            textPaint.getTextBounds(text, 0, text.length(), bounds);
            canvas.drawText(text, cx, cy - bounds.exactCenterY(), textPaint);
        }

        @Override public void setAlpha(int alpha) { circle.setAlpha(alpha); }
        @Override public void setColorFilter(android.graphics.ColorFilter cf) { circle.setColorFilter(cf); }
        @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
    }
}
