package com.nonsense.chat.data.repos;

import com.nonsense.chat.data.DocRepository;
import com.nonsense.chat.data.RealtimeBus;
import com.nonsense.chat.data.cache.DocCache;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
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
public final class ChatRepository_Factory implements Factory<ChatRepository> {
  private final Provider<DocRepository> docsProvider;

  private final Provider<RealtimeBus> realtimeProvider;

  private final Provider<DocCache> cacheProvider;

  public ChatRepository_Factory(Provider<DocRepository> docsProvider,
      Provider<RealtimeBus> realtimeProvider, Provider<DocCache> cacheProvider) {
    this.docsProvider = docsProvider;
    this.realtimeProvider = realtimeProvider;
    this.cacheProvider = cacheProvider;
  }

  @Override
  public ChatRepository get() {
    return newInstance(docsProvider.get(), realtimeProvider.get(), cacheProvider.get());
  }

  public static ChatRepository_Factory create(Provider<DocRepository> docsProvider,
      Provider<RealtimeBus> realtimeProvider, Provider<DocCache> cacheProvider) {
    return new ChatRepository_Factory(docsProvider, realtimeProvider, cacheProvider);
  }

  public static ChatRepository newInstance(DocRepository docs, RealtimeBus realtime,
      DocCache cache) {
    return new ChatRepository(docs, realtime, cache);
  }
}
