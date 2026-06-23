package com.nonsense.chat.push;

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
public final class PushTokenManager_Factory implements Factory<PushTokenManager> {
  private final Provider<DocRepository> docsProvider;

  public PushTokenManager_Factory(Provider<DocRepository> docsProvider) {
    this.docsProvider = docsProvider;
  }

  @Override
  public PushTokenManager get() {
    return newInstance(docsProvider.get());
  }

  public static PushTokenManager_Factory create(Provider<DocRepository> docsProvider) {
    return new PushTokenManager_Factory(docsProvider);
  }

  public static PushTokenManager newInstance(DocRepository docs) {
    return new PushTokenManager(docs);
  }
}
