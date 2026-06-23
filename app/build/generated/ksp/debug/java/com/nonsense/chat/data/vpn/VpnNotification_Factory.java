package com.nonsense.chat.data.vpn;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class VpnNotification_Factory implements Factory<VpnNotification> {
  @Override
  public VpnNotification get() {
    return newInstance();
  }

  public static VpnNotification_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static VpnNotification newInstance() {
    return new VpnNotification();
  }

  private static final class InstanceHolder {
    private static final VpnNotification_Factory INSTANCE = new VpnNotification_Factory();
  }
}
