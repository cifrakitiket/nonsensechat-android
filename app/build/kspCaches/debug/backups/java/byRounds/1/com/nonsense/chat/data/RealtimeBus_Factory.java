package com.nonsense.chat.data;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import io.github.jan.supabase.SupabaseClient;
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
public final class RealtimeBus_Factory implements Factory<RealtimeBus> {
  private final Provider<SupabaseClient> clientProvider;

  private final Provider<CoroutineScope> scopeProvider;

  public RealtimeBus_Factory(Provider<SupabaseClient> clientProvider,
      Provider<CoroutineScope> scopeProvider) {
    this.clientProvider = clientProvider;
    this.scopeProvider = scopeProvider;
  }

  @Override
  public RealtimeBus get() {
    return newInstance(clientProvider.get(), scopeProvider.get());
  }

  public static RealtimeBus_Factory create(Provider<SupabaseClient> clientProvider,
      Provider<CoroutineScope> scopeProvider) {
    return new RealtimeBus_Factory(clientProvider, scopeProvider);
  }

  public static RealtimeBus newInstance(SupabaseClient client, CoroutineScope scope) {
    return new RealtimeBus(client, scope);
  }
}
