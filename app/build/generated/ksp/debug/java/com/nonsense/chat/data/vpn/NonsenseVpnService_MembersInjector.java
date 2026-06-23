package com.nonsense.chat.data.vpn;

import com.nonsense.chat.data.SettingsStore;
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
public final class NonsenseVpnService_MembersInjector implements MembersInjector<NonsenseVpnService> {
  private final Provider<SettingsStore> settingsProvider;

  private final Provider<VpnStateRepository> statesProvider;

  private final Provider<SingBoxConfigBuilder> configBuilderProvider;

  private final Provider<VpnNotification> notificationProvider;

  private final Provider<TunnelEngine> engineProvider;

  public NonsenseVpnService_MembersInjector(Provider<SettingsStore> settingsProvider,
      Provider<VpnStateRepository> statesProvider,
      Provider<SingBoxConfigBuilder> configBuilderProvider,
      Provider<VpnNotification> notificationProvider, Provider<TunnelEngine> engineProvider) {
    this.settingsProvider = settingsProvider;
    this.statesProvider = statesProvider;
    this.configBuilderProvider = configBuilderProvider;
    this.notificationProvider = notificationProvider;
    this.engineProvider = engineProvider;
  }

  public static MembersInjector<NonsenseVpnService> create(Provider<SettingsStore> settingsProvider,
      Provider<VpnStateRepository> statesProvider,
      Provider<SingBoxConfigBuilder> configBuilderProvider,
      Provider<VpnNotification> notificationProvider, Provider<TunnelEngine> engineProvider) {
    return new NonsenseVpnService_MembersInjector(settingsProvider, statesProvider, configBuilderProvider, notificationProvider, engineProvider);
  }

  @Override
  public void injectMembers(NonsenseVpnService instance) {
    injectSettings(instance, settingsProvider.get());
    injectStates(instance, statesProvider.get());
    injectConfigBuilder(instance, configBuilderProvider.get());
    injectNotification(instance, notificationProvider.get());
    injectEngine(instance, engineProvider.get());
  }

  @InjectedFieldSignature("com.nonsense.chat.data.vpn.NonsenseVpnService.settings")
  public static void injectSettings(NonsenseVpnService instance, SettingsStore settings) {
    instance.settings = settings;
  }

  @InjectedFieldSignature("com.nonsense.chat.data.vpn.NonsenseVpnService.states")
  public static void injectStates(NonsenseVpnService instance, VpnStateRepository states) {
    instance.states = states;
  }

  @InjectedFieldSignature("com.nonsense.chat.data.vpn.NonsenseVpnService.configBuilder")
  public static void injectConfigBuilder(NonsenseVpnService instance,
      SingBoxConfigBuilder configBuilder) {
    instance.configBuilder = configBuilder;
  }

  @InjectedFieldSignature("com.nonsense.chat.data.vpn.NonsenseVpnService.notification")
  public static void injectNotification(NonsenseVpnService instance, VpnNotification notification) {
    instance.notification = notification;
  }

  @InjectedFieldSignature("com.nonsense.chat.data.vpn.NonsenseVpnService.engine")
  public static void injectEngine(NonsenseVpnService instance, TunnelEngine engine) {
    instance.engine = engine;
  }
}
