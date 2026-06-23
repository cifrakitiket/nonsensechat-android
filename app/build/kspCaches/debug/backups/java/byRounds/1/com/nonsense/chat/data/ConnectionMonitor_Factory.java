package com.nonsense.chat.data;

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
public final class ConnectionMonitor_Factory implements Factory<ConnectionMonitor> {
  @Override
  public ConnectionMonitor get() {
    return newInstance();
  }

  public static ConnectionMonitor_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static ConnectionMonitor newInstance() {
    return new ConnectionMonitor();
  }

  private static final class InstanceHolder {
    private static final ConnectionMonitor_Factory INSTANCE = new ConnectionMonitor_Factory();
  }
}
