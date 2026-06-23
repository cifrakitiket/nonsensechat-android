package com.nonsense.chat.data.cache;

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
public final class DocCache_Factory implements Factory<DocCache> {
  private final Provider<AppDatabase> dbProvider;

  public DocCache_Factory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public DocCache get() {
    return newInstance(dbProvider.get());
  }

  public static DocCache_Factory create(Provider<AppDatabase> dbProvider) {
    return new DocCache_Factory(dbProvider);
  }

  public static DocCache newInstance(AppDatabase db) {
    return new DocCache(db);
  }
}
