package com.nonsense.chat.ui.vpn;

import android.content.Context;
import com.nonsense.chat.data.SettingsStore;
import com.nonsense.chat.data.vpn.VpnStateRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class VpnViewModel_Factory implements Factory<VpnViewModel> {
  private final Provider<Context> contextProvider;

  private final Provider<SettingsStore> settingsProvider;

  private final Provider<VpnStateRepository> statesProvider;

  public VpnViewModel_Factory(Provider<Context> contextProvider,
      Provider<SettingsStore> settingsProvider, Provider<VpnStateRepository> statesProvider) {
    this.contextProvider = contextProvider;
    this.settingsProvider = settingsProvider;
    this.statesProvider = statesProvider;
  }

  @Override
  public VpnViewModel get() {
    return newInstance(contextProvider.get(), settingsProvider.get(), statesProvider.get());
  }

  public static VpnViewModel_Factory create(Provider<Context> contextProvider,
      Provider<SettingsStore> settingsProvider, Provider<VpnStateRepository> statesProvider) {
    return new VpnViewModel_Factory(contextProvider, settingsProvider, statesProvider);
  }

  public static VpnViewModel newInstance(Context context, SettingsStore settings,
      VpnStateRepository states) {
    return new VpnViewModel(context, settings, states);
  }
}
