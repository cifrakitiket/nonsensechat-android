package com.nonsense.chat.data.repos;

import com.nonsense.chat.data.DocRepository;
import com.nonsense.chat.data.RealtimeBus;
import com.nonsense.chat.data.cache.DocCache;
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
public final class UserRepository_Factory implements Factory<UserRepository> {
  private final Provider<SupabaseClient> clientProvider;

  private final Provider<DocRepository> docsProvider;

  private final Provider<RealtimeBus> realtimeProvider;

  private final Provider<DocCache> cacheProvider;

  public UserRepository_Factory(Provider<SupabaseClient> clientProvider,
      Provider<DocRepository> docsProvider, Provider<RealtimeBus> realtimeProvider,
      Provider<DocCache> cacheProvider) {
    this.clientProvider = clientProvider;
    this.docsProvider = docsProvider;
    this.realtimeProvider = realtimeProvider;
    this.cacheProvider = cacheProvider;
  }

  @Override
  public UserRepository get() {
    return newInstance(clientProvider.get(), docsProvider.get(), realtimeProvider.get(), cacheProvider.get());
  }

  public static UserRepository_Factory create(Provider<SupabaseClient> clientProvider,
      Provider<DocRepository> docsProvider, Provider<RealtimeBus> realtimeProvider,
      Provider<DocCache> cacheProvider) {
    return new UserRepository_Factory(clientProvider, docsProvider, realtimeProvider, cacheProvider);
  }

  public static UserRepository newInstance(SupabaseClient client, DocRepository docs,
      RealtimeBus realtime, DocCache cache) {
    return new UserRepository(client, docs, realtime, cache);
  }
}
