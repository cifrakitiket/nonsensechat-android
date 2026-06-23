package com.nonsense.chat.call;

import com.nonsense.chat.data.AccountManager;
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
public final class CallViewModel_Factory implements Factory<CallViewModel> {
  private final Provider<CallManager> managerProvider;

  private final Provider<UserRepository> usersProvider;

  private final Provider<AccountManager> accountProvider;

  public CallViewModel_Factory(Provider<CallManager> managerProvider,
      Provider<UserRepository> usersProvider, Provider<AccountManager> accountProvider) {
    this.managerProvider = managerProvider;
    this.usersProvider = usersProvider;
    this.accountProvider = accountProvider;
  }

  @Override
  public CallViewModel get() {
    return newInstance(managerProvider.get(), usersProvider.get(), accountProvider.get());
  }

  public static CallViewModel_Factory create(Provider<CallManager> managerProvider,
      Provider<UserRepository> usersProvider, Provider<AccountManager> accountProvider) {
    return new CallViewModel_Factory(managerProvider, usersProvider, accountProvider);
  }

  public static CallViewModel newInstance(CallManager manager, UserRepository users,
      AccountManager account) {
    return new CallViewModel(manager, users, account);
  }
}
