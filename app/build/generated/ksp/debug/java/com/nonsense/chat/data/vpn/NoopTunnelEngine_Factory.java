package com.nonsense.chat.data.vpn;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
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
public final class NoopTunnelEngine_Factory implements Factory<NoopTunnelEngine> {
  @Override
  public NoopTunnelEngine get() {
    return newInstance();
  }

  public static NoopTunnelEngine_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static NoopTunnelEngine newInstance() {
    return new NoopTunnelEngine();
  }

  private static final class InstanceHolder {
    private static final NoopTunnelEngine_Factory INSTANCE = new NoopTunnelEngine_Factory();
  }
}
