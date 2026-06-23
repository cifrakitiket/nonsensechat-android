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
public final class FolderRepository_Factory implements Factory<FolderRepository> {
  private final Provider<DocRepository> docsProvider;

  private final Provider<RealtimeBus> realtimeProvider;

  public FolderRepository_Factory(Provider<DocRepository> docsProvider,
      Provider<RealtimeBus> realtimeProvider) {
    this.docsProvider = docsProvider;
    this.realtimeProvider = realtimeProvider;
  }

  @Override
  public FolderRepository get() {
    return newInstance(docsProvider.get(), realtimeProvider.get());
  }

  public static FolderRepository_Factory create(Provider<DocRepository> docsProvider,
      Provider<RealtimeBus> realtimeProvider) {
    return new FolderRepository_Factory(docsProvider, realtimeProvider);
  }

  public static FolderRepository newInstance(DocRepository docs, RealtimeBus realtime) {
    return new FolderRepository(docs, realtime);
  }
}
