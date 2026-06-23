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
public final class MessageRepository_Factory implements Factory<MessageRepository> {
  private final Provider<DocRepository> docsProvider;

  private final Provider<RealtimeBus> realtimeProvider;

  private final Provider<DocCache> cacheProvider;

  public MessageRepository_Factory(Provider<DocRepository> docsProvider,
      Provider<RealtimeBus> realtimeProvider, Provider<DocCache> cacheProvider) {
    this.docsProvider = docsProvider;
    this.realtimeProvider = realtimeProvider;
    this.cacheProvider = cacheProvider;
  }

  @Override
  public MessageRepository get() {
    return newInstance(docsProvider.get(), realtimeProvider.get(), cacheProvider.get());
  }

  public static MessageRepository_Factory create(Provider<DocRepository> docsProvider,
      Provider<RealtimeBus> realtimeProvider, Provider<DocCache> cacheProvider) {
    return new MessageRepository_Factory(docsProvider, realtimeProvider, cacheProvider);
  }

  public static MessageRepository newInstance(DocRepository docs, RealtimeBus realtime,
      DocCache cache) {
    return new MessageRepository(docs, realtime, cache);
  }
}
