package com.nonsense.chat;

import coil.ImageLoader;
import com.nonsense.chat.data.AccountManager;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class NonsenseApp_MembersInjector implements MembersInjector<NonsenseApp> {
  private final Provider<ImageLoader> imageLoaderProvider;

  private final Provider<AccountManager> accountProvider;

  public NonsenseApp_MembersInjector(Provider<ImageLoader> imageLoaderProvider,
      Provider<AccountManager> accountProvider) {
    this.imageLoaderProvider = imageLoaderProvider;
    this.accountProvider = accountProvider;
  }

  public static MembersInjector<NonsenseApp> create(Provider<ImageLoader> imageLoaderProvider,
      Provider<AccountManager> accountProvider) {
    return new NonsenseApp_MembersInjector(imageLoaderProvider, accountProvider);
  }

  @Override
  public void injectMembers(NonsenseApp instance) {
    injectImageLoader(instance, imageLoaderProvider.get());
    injectAccount(instance, accountProvider.get());
  }

  @InjectedFieldSignature("com.nonsense.chat.NonsenseApp.imageLoader")
  public static void injectImageLoader(NonsenseApp instance, ImageLoader imageLoader) {
    instance.imageLoader = imageLoader;
  }

  @InjectedFieldSignature("com.nonsense.chat.NonsenseApp.account")
  public static void injectAccount(NonsenseApp instance, AccountManager account) {
    instance.account = account;
  }
}
