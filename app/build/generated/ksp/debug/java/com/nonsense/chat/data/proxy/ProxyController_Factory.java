package com.nonsense.chat.data.proxy;

import com.nonsense.chat.data.SettingsStore;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import kotlinx.coroutines.CoroutineScope;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("com.nonsense.chat.di.AppScope")
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
public final class ProxyController_Factory implements Factory<ProxyController> {
  private final Provider<SettingsStore> settingsProvider;

  private final Provider<CoroutineScope> scopeProvider;

  public ProxyController_Factory(Provider<SettingsStore> settingsProvider,
      Provider<CoroutineScope> scopeProvider) {
    this.settingsProvider = settingsProvider;
    this.scopeProvider = scopeProvider;
  }

  @Override
  public ProxyController get() {
    return newInstance(settingsProvider.get(), scopeProvider.get());
  }

  public static ProxyController_Factory create(Provider<SettingsStore> settingsProvider,
      Provider<CoroutineScope> scopeProvider) {
    return new ProxyController_Factory(settingsProvider, scopeProvider);
  }

  public static ProxyController newInstance(SettingsStore settings, CoroutineScope scope) {
    return new ProxyController(settings, scope);
  }
}
