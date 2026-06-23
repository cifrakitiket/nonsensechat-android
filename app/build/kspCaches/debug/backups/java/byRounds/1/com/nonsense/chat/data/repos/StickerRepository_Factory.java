package com.nonsense.chat.data.repos;

import com.nonsense.chat.data.DocRepository;
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
public final class StickerRepository_Factory implements Factory<StickerRepository> {
  private final Provider<DocRepository> docsProvider;

  public StickerRepository_Factory(Provider<DocRepository> docsProvider) {
    this.docsProvider = docsProvider;
  }

  @Override
  public StickerRepository get() {
    return newInstance(docsProvider.get());
  }

  public static StickerRepository_Factory create(Provider<DocRepository> docsProvider) {
    return new StickerRepository_Factory(docsProvider);
  }

  public static StickerRepository newInstance(DocRepository docs) {
    return new StickerRepository(docs);
  }
}
