package com.nonsense.chat.data.repos;

import com.nonsense.chat.data.DocRepository;
import com.nonsense.chat.data.RealtimeBus;
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
public final class FriendRepository_Factory implements Factory<FriendRepository> {
  private final Provider<DocRepository> docsProvider;

  private final Provider<RealtimeBus> realtimeProvider;

  public FriendRepository_Factory(Provider<DocRepository> docsProvider,
      Provider<RealtimeBus> realtimeProvider) {
    this.docsProvider = docsProvider;
    this.realtimeProvider = realtimeProvider;
  }

  @Override
  public FriendRepository get() {
    return newInstance(docsProvider.get(), realtimeProvider.get());
  }

  public static FriendRepository_Factory create(Provider<DocRepository> docsProvider,
      Provider<RealtimeBus> realtimeProvider) {
    return new FriendRepository_Factory(docsProvider, realtimeProvider);
  }

  public static FriendRepository newInstance(DocRepository docs, RealtimeBus realtime) {
    return new FriendRepository(docs, realtime);
  }
}
