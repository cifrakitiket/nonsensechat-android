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
public final class PresenceRepository_Factory implements Factory<PresenceRepository> {
  private final Provider<DocRepository> docsProvider;

  public PresenceRepository_Factory(Provider<DocRepository> docsProvider) {
    this.docsProvider = docsProvider;
  }

  @Override
  public PresenceRepository get() {
    return newInstance(docsProvider.get());
  }

  public static PresenceRepository_Factory create(Provider<DocRepository> docsProvider) {
    return new PresenceRepository_Factory(docsProvider);
  }

  public static PresenceRepository newInstance(DocRepository docs) {
    return new PresenceRepository(docs);
  }
}
