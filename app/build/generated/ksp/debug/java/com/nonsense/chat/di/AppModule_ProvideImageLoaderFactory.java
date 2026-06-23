package com.nonsense.chat.di;

import android.content.Context;
import coil.ImageLoader;
import com.nonsense.chat.data.proxy.ProxyController;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation"
})
public final class AppModule_ProvideImageLoaderFactory implements Factory<ImageLoader> {
  private final Provider<Context> contextProvider;

  private final Provider<ProxyController> proxyControllerProvider;

  public AppModule_ProvideImageLoaderFactory(Provider<Context> contextProvider,
      Provider<ProxyController> proxyControllerProvider) {
    this.contextProvider = contextProvider;
    this.proxyControllerProvider = proxyControllerProvider;
  }

  @Override
  public ImageLoader get() {
    return provideImageLoader(contextProvider.get(), proxyControllerProvider.get());
  }

  public static AppModule_ProvideImageLoaderFactory create(Provider<Context> contextProvider,
      Provider<ProxyController> proxyControllerProvider) {
    return new AppModule_ProvideImageLoaderFactory(contextProvider, proxyControllerProvider);
  }

  public static ImageLoader provideImageLoader(Context context, ProxyController proxyController) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideImageLoader(context, proxyController));
  }
}
