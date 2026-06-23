package com.nonsense.chat.push;

import com.nonsense.chat.data.SettingsStore;
import com.nonsense.chat.data.repos.AuthRepository;
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
public final class FcmService_MembersInjector implements MembersInjector<FcmService> {
  private final Provider<PushTokenManager> pushTokensProvider;

  private final Provider<AuthRepository> authProvider;

  private final Provider<SettingsStore> settingsProvider;

  public FcmService_MembersInjector(Provider<PushTokenManager> pushTokensProvider,
      Provider<AuthRepository> authProvider, Provider<SettingsStore> settingsProvider) {
    this.pushTokensProvider = pushTokensProvider;
    this.authProvider = authProvider;
    this.settingsProvider = settingsProvider;
  }

  public static MembersInjector<FcmService> create(Provider<PushTokenManager> pushTokensProvider,
      Provider<AuthRepository> authProvider, Provider<SettingsStore> settingsProvider) {
    return new FcmService_MembersInjector(pushTokensProvider, authProvider, settingsProvider);
  }

  @Override
  public void injectMembers(FcmService instance) {
    injectPushTokens(instance, pushTokensProvider.get());
    injectAuth(instance, authProvider.get());
    injectSettings(instance, settingsProvider.get());
  }

  @InjectedFieldSignature("com.nonsense.chat.push.FcmService.pushTokens")
  public static void injectPushTokens(FcmService instance, PushTokenManager pushTokens) {
    instance.pushTokens = pushTokens;
  }

  @InjectedFieldSignature("com.nonsense.chat.push.FcmService.auth")
  public static void injectAuth(FcmService instance, AuthRepository auth) {
    instance.auth = auth;
  }

  @InjectedFieldSignature("com.nonsense.chat.push.FcmService.settings")
  public static void injectSettings(FcmService instance, SettingsStore settings) {
    instance.settings = settings;
  }
}
