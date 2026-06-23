package com.nonsense.chat.ui.settings;

import com.nonsense.chat.data.AccountManager;
import com.nonsense.chat.data.SettingsStore;
import com.nonsense.chat.data.repos.UserRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<AccountManager> accountProvider;

  private final Provider<SettingsStore> settingsProvider;

  private final Provider<UserRepository> usersProvider;

  public SettingsViewModel_Factory(Provider<AccountManager> accountProvider,
      Provider<SettingsStore> settingsProvider, Provider<UserRepository> usersProvider) {
    this.accountProvider = accountProvider;
    this.settingsProvider = settingsProvider;
    this.usersProvider = usersProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(accountProvider.get(), settingsProvider.get(), usersProvider.get());
  }

  public static SettingsViewModel_Factory create(Provider<AccountManager> accountProvider,
      Provider<SettingsStore> settingsProvider, Provider<UserRepository> usersProvider) {
    return new SettingsViewModel_Factory(accountProvider, settingsProvider, usersProvider);
  }

  public static SettingsViewModel newInstance(AccountManager account, SettingsStore settings,
      UserRepository users) {
    return new SettingsViewModel(account, settings, users);
  }
}
