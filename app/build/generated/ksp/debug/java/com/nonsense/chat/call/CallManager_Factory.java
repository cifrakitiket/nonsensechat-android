package com.nonsense.chat.call;

import android.content.Context;
import com.nonsense.chat.data.AccountManager;
import com.nonsense.chat.data.repos.CallRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import kotlinx.coroutines.CoroutineScope;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata({
    "dagger.hilt.android.qualifiers.ApplicationContext",
    "com.nonsense.chat.di.AppScope"
})
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
public final class CallManager_Factory implements Factory<CallManager> {
  private final Provider<Context> contextProvider;

  private final Provider<CallRepository> callsProvider;

  private final Provider<AccountManager> accountProvider;

  private final Provider<CoroutineScope> scopeProvider;

  public CallManager_Factory(Provider<Context> contextProvider,
      Provider<CallRepository> callsProvider, Provider<AccountManager> accountProvider,
      Provider<CoroutineScope> scopeProvider) {
    this.contextProvider = contextProvider;
    this.callsProvider = callsProvider;
    this.accountProvider = accountProvider;
    this.scopeProvider = scopeProvider;
  }

  @Override
  public CallManager get() {
    return newInstance(contextProvider.get(), callsProvider.get(), accountProvider.get(), scopeProvider.get());
  }

  public static CallManager_Factory create(Provider<Context> contextProvider,
      Provider<CallRepository> callsProvider, Provider<AccountManager> accountProvider,
      Provider<CoroutineScope> scopeProvider) {
    return new CallManager_Factory(contextProvider, callsProvider, accountProvider, scopeProvider);
  }

  public static CallManager newInstance(Context context, CallRepository calls,
      AccountManager account, CoroutineScope scope) {
    return new CallManager(context, calls, account, scope);
  }
}
