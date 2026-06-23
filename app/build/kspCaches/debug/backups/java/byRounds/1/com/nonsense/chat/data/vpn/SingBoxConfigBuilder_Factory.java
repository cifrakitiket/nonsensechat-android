package com.nonsense.chat.data.vpn;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import kotlinx.serialization.json.Json;

@ScopeMetadata
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
public final class SingBoxConfigBuilder_Factory implements Factory<SingBoxConfigBuilder> {
  private final Provider<Json> jsonProvider;

  public SingBoxConfigBuilder_Factory(Provider<Json> jsonProvider) {
    this.jsonProvider = jsonProvider;
  }

  @Override
  public SingBoxConfigBuilder get() {
    return newInstance(jsonProvider.get());
  }

  public static SingBoxConfigBuilder_Factory create(Provider<Json> jsonProvider) {
    return new SingBoxConfigBuilder_Factory(jsonProvider);
  }

  public static SingBoxConfigBuilder newInstance(Json json) {
    return new SingBoxConfigBuilder(json);
  }
}
