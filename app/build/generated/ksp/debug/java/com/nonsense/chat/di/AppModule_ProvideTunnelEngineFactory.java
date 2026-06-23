package com.nonsense.chat.di;

import com.nonsense.chat.data.vpn.NoopTunnelEngine;
import com.nonsense.chat.data.vpn.TunnelEngine;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
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
public final class AppModule_ProvideTunnelEngineFactory implements Factory<TunnelEngine> {
  private final Provider<NoopTunnelEngine> engineProvider;

  public AppModule_ProvideTunnelEngineFactory(Provider<NoopTunnelEngine> engineProvider) {
    this.engineProvider = engineProvider;
  }

  @Override
  public TunnelEngine get() {
    return provideTunnelEngine(engineProvider.get());
  }

  public static AppModule_ProvideTunnelEngineFactory create(
      Provider<NoopTunnelEngine> engineProvider) {
    return new AppModule_ProvideTunnelEngineFactory(engineProvider);
  }

  public static TunnelEngine provideTunnelEngine(NoopTunnelEngine engine) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideTunnelEngine(engine));
  }
}
