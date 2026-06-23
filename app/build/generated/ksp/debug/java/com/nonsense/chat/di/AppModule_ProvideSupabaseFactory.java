package com.nonsense.chat.di;

import com.nonsense.chat.data.proxy.ProxyController;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import io.github.jan.supabase.SupabaseClient;
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
public final class AppModule_ProvideSupabaseFactory implements Factory<SupabaseClient> {
  private final Provider<ProxyController> proxyControllerProvider;

  public AppModule_ProvideSupabaseFactory(Provider<ProxyController> proxyControllerProvider) {
    this.proxyControllerProvider = proxyControllerProvider;
  }

  @Override
  public SupabaseClient get() {
    return provideSupabase(proxyControllerProvider.get());
  }

  public static AppModule_ProvideSupabaseFactory create(
      Provider<ProxyController> proxyControllerProvider) {
    return new AppModule_ProvideSupabaseFactory(proxyControllerProvider);
  }

  public static SupabaseClient provideSupabase(ProxyController proxyController) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideSupabase(proxyController));
  }
}
