package com.nonsense.chat;

import com.nonsense.chat.data.SettingsStore;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class MainActivity_MembersInjector implements MembersInjector<MainActivity> {
  private final Provider<SettingsStore> settingsProvider;

  public MainActivity_MembersInjector(Provider<SettingsStore> settingsProvider) {
    this.settingsProvider = settingsProvider;
  }

  public static MembersInjector<MainActivity> create(Provider<SettingsStore> settingsProvider) {
    return new MainActivity_MembersInjector(settingsProvider);
  }

  @Override
  public void injectMembers(MainActivity instance) {
    injectSettings(instance, settingsProvider.get());
  }

  @InjectedFieldSignature("com.nonsense.chat.MainActivity.settings")
  public static void injectSettings(MainActivity instance, SettingsStore settings) {
    instance.settings = settings;
  }
}
