package com.nonsense.chat.data;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import io.github.jan.supabase.SupabaseClient;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class StorageRepository_Factory implements Factory<StorageRepository> {
  private final Provider<SupabaseClient> clientProvider;

  private final Provider<Context> contextProvider;

  public StorageRepository_Factory(Provider<SupabaseClient> clientProvider,
      Provider<Context> contextProvider) {
    this.clientProvider = clientProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public StorageRepository get() {
    return newInstance(clientProvider.get(), contextProvider.get());
  }

  public static StorageRepository_Factory create(Provider<SupabaseClient> clientProvider,
      Provider<Context> contextProvider) {
    return new StorageRepository_Factory(clientProvider, contextProvider);
  }

  public static StorageRepository newInstance(SupabaseClient client, Context context) {
    return new StorageRepository(client, context);
  }
}
