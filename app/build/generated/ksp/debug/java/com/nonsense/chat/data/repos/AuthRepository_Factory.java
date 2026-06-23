package com.nonsense.chat.data.repos;

import com.nonsense.chat.data.DocRepository;
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
public final class AuthRepository_Factory implements Factory<AuthRepository> {
  private final Provider<SupabaseClient> clientProvider;

  private final Provider<DocRepository> docsProvider;

  public AuthRepository_Factory(Provider<SupabaseClient> clientProvider,
      Provider<DocRepository> docsProvider) {
    this.clientProvider = clientProvider;
    this.docsProvider = docsProvider;
  }

  @Override
  public AuthRepository get() {
    return newInstance(clientProvider.get(), docsProvider.get());
  }

  public static AuthRepository_Factory create(Provider<SupabaseClient> clientProvider,
      Provider<DocRepository> docsProvider) {
    return new AuthRepository_Factory(clientProvider, docsProvider);
  }

  public static AuthRepository newInstance(SupabaseClient client, DocRepository docs) {
    return new AuthRepository(client, docs);
  }
}
