package com.nonsense.chat.ui.navigation;

import com.nonsense.chat.data.AccountManager;
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
public final class RootViewModel_Factory implements Factory<RootViewModel> {
  private final Provider<AccountManager> accountProvider;

  public RootViewModel_Factory(Provider<AccountManager> accountProvider) {
    this.accountProvider = accountProvider;
  }

  @Override
  public RootViewModel get() {
    return newInstance(accountProvider.get());
  }

  public static RootViewModel_Factory create(Provider<AccountManager> accountProvider) {
    return new RootViewModel_Factory(accountProvider);
  }

  public static RootViewModel newInstance(AccountManager account) {
    return new RootViewModel(account);
  }
}
