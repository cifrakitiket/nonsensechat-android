package com.nonsense.chat.data;

import com.nonsense.chat.data.repos.AuthRepository;
import com.nonsense.chat.data.repos.PresenceRepository;
import com.nonsense.chat.data.repos.UserRepository;
import com.nonsense.chat.push.PushTokenManager;
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
public final class AccountManager_Factory implements Factory<AccountManager> {
  private final Provider<AuthRepository> authProvider;

  private final Provider<UserRepository> usersProvider;

  private final Provider<PresenceRepository> presenceProvider;

  private final Provider<PushTokenManager> pushTokensProvider;

  private final Provider<CoroutineScope> scopeProvider;

  public AccountManager_Factory(Provider<AuthRepository> authProvider,
      Provider<UserRepository> usersProvider, Provider<PresenceRepository> presenceProvider,
      Provider<PushTokenManager> pushTokensProvider, Provider<CoroutineScope> scopeProvider) {
    this.authProvider = authProvider;
    this.usersProvider = usersProvider;
    this.presenceProvider = presenceProvider;
    this.pushTokensProvider = pushTokensProvider;
    this.scopeProvider = scopeProvider;
  }

  @Override
  public AccountManager get() {
    return newInstance(authProvider.get(), usersProvider.get(), presenceProvider.get(), pushTokensProvider.get(), scopeProvider.get());
  }

  public static AccountManager_Factory create(Provider<AuthRepository> authProvider,
      Provider<UserRepository> usersProvider, Provider<PresenceRepository> presenceProvider,
      Provider<PushTokenManager> pushTokensProvider, Provider<CoroutineScope> scopeProvider) {
    return new AccountManager_Factory(authProvider, usersProvider, presenceProvider, pushTokensProvider, scopeProvider);
  }

  public static AccountManager newInstance(AuthRepository auth, UserRepository users,
      PresenceRepository presence, PushTokenManager pushTokens, CoroutineScope scope) {
    return new AccountManager(auth, users, presence, pushTokens, scope);
  }
}
