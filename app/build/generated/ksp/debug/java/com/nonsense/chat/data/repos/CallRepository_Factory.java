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
public final class CallRepository_Factory implements Factory<CallRepository> {
  private final Provider<DocRepository> docsProvider;

  private final Provider<RealtimeBus> realtimeProvider;

  public CallRepository_Factory(Provider<DocRepository> docsProvider,
      Provider<RealtimeBus> realtimeProvider) {
    this.docsProvider = docsProvider;
    this.realtimeProvider = realtimeProvider;
  }

  @Override
  public CallRepository get() {
    return newInstance(docsProvider.get(), realtimeProvider.get());
  }

  public static CallRepository_Factory create(Provider<DocRepository> docsProvider,
      Provider<RealtimeBus> realtimeProvider) {
    return new CallRepository_Factory(docsProvider, realtimeProvider);
  }

  public static CallRepository newInstance(DocRepository docs, RealtimeBus realtime) {
    return new CallRepository(docs, realtime);
  }
}
