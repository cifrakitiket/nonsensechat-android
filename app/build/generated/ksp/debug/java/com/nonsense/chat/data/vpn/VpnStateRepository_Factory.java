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
public final class VpnStateRepository_Factory implements Factory<VpnStateRepository> {
  @Override
  public VpnStateRepository get() {
    return newInstance();
  }

  public static VpnStateRepository_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static VpnStateRepository newInstance() {
    return new VpnStateRepository();
  }

  private static final class InstanceHolder {
    private static final VpnStateRepository_Factory INSTANCE = new VpnStateRepository_Factory();
  }
}
