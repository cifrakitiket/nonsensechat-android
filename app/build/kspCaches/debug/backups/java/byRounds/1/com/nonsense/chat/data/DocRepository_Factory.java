package com.nonsense.chat.data;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import io.github.jan.supabase.SupabaseClient;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class DocRepository_Factory implements Factory<DocRepository> {
  private final Provider<SupabaseClient> clientProvider;

  private final Provider<ConnectionMonitor> connectionProvider;

  public DocRepository_Factory(Provider<SupabaseClient> clientProvider,
      Provider<ConnectionMonitor> connectionProvider) {
    this.clientProvider = clientProvider;
    this.connectionProvider = connectionProvider;
  }

  @Override
  public DocRepository get() {
    return newInstance(clientProvider.get(), connectionProvider.get());
  }

  public static DocRepository_Factory create(Provider<SupabaseClient> clientProvider,
      Provider<ConnectionMonitor> connectionProvider) {
    return new DocRepository_Factory(clientProvider, connectionProvider);
  }

  public static DocRepository newInstance(SupabaseClient client, ConnectionMonitor connection) {
    return new DocRepository(client, connection);
  }
}
