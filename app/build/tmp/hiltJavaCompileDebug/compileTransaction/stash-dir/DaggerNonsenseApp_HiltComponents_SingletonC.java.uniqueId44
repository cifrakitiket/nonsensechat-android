package com.nonsense.chat;

import android.app.Activity;
import android.app.Service;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import coil.ImageLoader;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.nonsense.chat.call.CallManager;
import com.nonsense.chat.call.CallViewModel;
import com.nonsense.chat.call.CallViewModel_HiltModules;
import com.nonsense.chat.data.AccountManager;
import com.nonsense.chat.data.ConnectionMonitor;
import com.nonsense.chat.data.DocRepository;
import com.nonsense.chat.data.RealtimeBus;
import com.nonsense.chat.data.SettingsStore;
import com.nonsense.chat.data.StorageRepository;
import com.nonsense.chat.data.cache.AppDatabase;
import com.nonsense.chat.data.cache.DocCache;
import com.nonsense.chat.data.proxy.ProxyController;
import com.nonsense.chat.data.repos.AuthRepository;
import com.nonsense.chat.data.repos.CallRepository;
import com.nonsense.chat.data.repos.ChatRepository;
import com.nonsense.chat.data.repos.FolderRepository;
import com.nonsense.chat.data.repos.FriendRepository;
import com.nonsense.chat.data.repos.MessageRepository;
import com.nonsense.chat.data.repos.PresenceRepository;
import com.nonsense.chat.data.repos.StickerRepository;
import com.nonsense.chat.data.repos.UserRepository;
import com.nonsense.chat.data.vpn.NonsenseVpnService;
import com.nonsense.chat.data.vpn.NonsenseVpnService_MembersInjector;
import com.nonsense.chat.data.vpn.NoopTunnelEngine;
import com.nonsense.chat.data.vpn.SingBoxConfigBuilder;
import com.nonsense.chat.data.vpn.TunnelEngine;
import com.nonsense.chat.data.vpn.VpnNotification;
import com.nonsense.chat.data.vpn.VpnStateRepository;
import com.nonsense.chat.di.AppModule_ProvideAppDatabaseFactory;
import com.nonsense.chat.di.AppModule_ProvideAppScopeFactory;
import com.nonsense.chat.di.AppModule_ProvideImageLoaderFactory;
import com.nonsense.chat.di.AppModule_ProvideJsonFactory;
import com.nonsense.chat.di.AppModule_ProvideSupabaseFactory;
import com.nonsense.chat.di.AppModule_ProvideTunnelEngineFactory;
import com.nonsense.chat.push.FcmService;
import com.nonsense.chat.push.FcmService_MembersInjector;
import com.nonsense.chat.push.PushTokenManager;
import com.nonsense.chat.ui.auth.AuthViewModel;
import com.nonsense.chat.ui.auth.AuthViewModel_HiltModules;
import com.nonsense.chat.ui.chat.ChatViewModel;
import com.nonsense.chat.ui.chat.ChatViewModel_HiltModules;
import com.nonsense.chat.ui.chat.StickerPackViewModel;
import com.nonsense.chat.ui.chat.StickerPackViewModel_HiltModules;
import com.nonsense.chat.ui.chat.StickerPickerViewModel;
import com.nonsense.chat.ui.chat.StickerPickerViewModel_HiltModules;
import com.nonsense.chat.ui.chatlist.ChatListViewModel;
import com.nonsense.chat.ui.chatlist.ChatListViewModel_HiltModules;
import com.nonsense.chat.ui.friends.FriendsViewModel;
import com.nonsense.chat.ui.friends.FriendsViewModel_HiltModules;
import com.nonsense.chat.ui.group.GroupInfoViewModel;
import com.nonsense.chat.ui.group.GroupInfoViewModel_HiltModules;
import com.nonsense.chat.ui.group.NewGroupViewModel;
import com.nonsense.chat.ui.group.NewGroupViewModel_HiltModules;
import com.nonsense.chat.ui.navigation.RootViewModel;
import com.nonsense.chat.ui.navigation.RootViewModel_HiltModules;
import com.nonsense.chat.ui.profile.EditProfileViewModel;
import com.nonsense.chat.ui.profile.EditProfileViewModel_HiltModules;
import com.nonsense.chat.ui.profile.ProfileViewModel;
import com.nonsense.chat.ui.profile.ProfileViewModel_HiltModules;
import com.nonsense.chat.ui.settings.SettingsViewModel;
import com.nonsense.chat.ui.settings.SettingsViewModel_HiltModules;
import com.nonsense.chat.ui.vpn.VpnViewModel;
import com.nonsense.chat.ui.vpn.VpnViewModel_HiltModules;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.IdentifierNameString;
import dagger.internal.KeepFieldType;
import dagger.internal.LazyClassKeyMap;
import dagger.internal.MapBuilder;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import io.github.jan.supabase.SupabaseClient;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.serialization.json.Json;

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
public final class DaggerNonsenseApp_HiltComponents_SingletonC {
  private DaggerNonsenseApp_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public NonsenseApp_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements NonsenseApp_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public NonsenseApp_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements NonsenseApp_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public NonsenseApp_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements NonsenseApp_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public NonsenseApp_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements NonsenseApp_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public NonsenseApp_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements NonsenseApp_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public NonsenseApp_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements NonsenseApp_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public NonsenseApp_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements NonsenseApp_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public NonsenseApp_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends NonsenseApp_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    private ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends NonsenseApp_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    private FragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends NonsenseApp_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    private ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends NonsenseApp_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    private ActivityCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public void injectMainActivity(MainActivity mainActivity) {
      injectMainActivity2(mainActivity);
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Map<Class<?>, Boolean> getViewModelKeys() {
      return LazyClassKeyMap.<Boolean>of(MapBuilder.<String, Boolean>newMapBuilder(14).put(LazyClassKeyProvider.com_nonsense_chat_ui_auth_AuthViewModel, AuthViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_nonsense_chat_call_CallViewModel, CallViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_nonsense_chat_ui_chatlist_ChatListViewModel, ChatListViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_nonsense_chat_ui_chat_ChatViewModel, ChatViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_nonsense_chat_ui_profile_EditProfileViewModel, EditProfileViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_nonsense_chat_ui_friends_FriendsViewModel, FriendsViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_nonsense_chat_ui_group_GroupInfoViewModel, GroupInfoViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_nonsense_chat_ui_group_NewGroupViewModel, NewGroupViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_nonsense_chat_ui_profile_ProfileViewModel, ProfileViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_nonsense_chat_ui_navigation_RootViewModel, RootViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_nonsense_chat_ui_settings_SettingsViewModel, SettingsViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_nonsense_chat_ui_chat_StickerPackViewModel, StickerPackViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_nonsense_chat_ui_chat_StickerPickerViewModel, StickerPickerViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_nonsense_chat_ui_vpn_VpnViewModel, VpnViewModel_HiltModules.KeyModule.provide()).build());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @CanIgnoreReturnValue
    private MainActivity injectMainActivity2(MainActivity instance) {
      MainActivity_MembersInjector.injectSettings(instance, singletonCImpl.settingsStoreProvider.get());
      return instance;
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_nonsense_chat_ui_group_NewGroupViewModel = "com.nonsense.chat.ui.group.NewGroupViewModel";

      static String com_nonsense_chat_ui_profile_EditProfileViewModel = "com.nonsense.chat.ui.profile.EditProfileViewModel";

      static String com_nonsense_chat_ui_chat_StickerPackViewModel = "com.nonsense.chat.ui.chat.StickerPackViewModel";

      static String com_nonsense_chat_ui_settings_SettingsViewModel = "com.nonsense.chat.ui.settings.SettingsViewModel";

      static String com_nonsense_chat_ui_chat_ChatViewModel = "com.nonsense.chat.ui.chat.ChatViewModel";

      static String com_nonsense_chat_ui_navigation_RootViewModel = "com.nonsense.chat.ui.navigation.RootViewModel";

      static String com_nonsense_chat_ui_chatlist_ChatListViewModel = "com.nonsense.chat.ui.chatlist.ChatListViewModel";

      static String com_nonsense_chat_ui_friends_FriendsViewModel = "com.nonsense.chat.ui.friends.FriendsViewModel";

      static String com_nonsense_chat_call_CallViewModel = "com.nonsense.chat.call.CallViewModel";

      static String com_nonsense_chat_ui_vpn_VpnViewModel = "com.nonsense.chat.ui.vpn.VpnViewModel";

      static String com_nonsense_chat_ui_group_GroupInfoViewModel = "com.nonsense.chat.ui.group.GroupInfoViewModel";

      static String com_nonsense_chat_ui_auth_AuthViewModel = "com.nonsense.chat.ui.auth.AuthViewModel";

      static String com_nonsense_chat_ui_profile_ProfileViewModel = "com.nonsense.chat.ui.profile.ProfileViewModel";

      static String com_nonsense_chat_ui_chat_StickerPickerViewModel = "com.nonsense.chat.ui.chat.StickerPickerViewModel";

      @KeepFieldType
      NewGroupViewModel com_nonsense_chat_ui_group_NewGroupViewModel2;

      @KeepFieldType
      EditProfileViewModel com_nonsense_chat_ui_profile_EditProfileViewModel2;

      @KeepFieldType
      StickerPackViewModel com_nonsense_chat_ui_chat_StickerPackViewModel2;

      @KeepFieldType
      SettingsViewModel com_nonsense_chat_ui_settings_SettingsViewModel2;

      @KeepFieldType
      ChatViewModel com_nonsense_chat_ui_chat_ChatViewModel2;

      @KeepFieldType
      RootViewModel com_nonsense_chat_ui_navigation_RootViewModel2;

      @KeepFieldType
      ChatListViewModel com_nonsense_chat_ui_chatlist_ChatListViewModel2;

      @KeepFieldType
      FriendsViewModel com_nonsense_chat_ui_friends_FriendsViewModel2;

      @KeepFieldType
      CallViewModel com_nonsense_chat_call_CallViewModel2;

      @KeepFieldType
      VpnViewModel com_nonsense_chat_ui_vpn_VpnViewModel2;

      @KeepFieldType
      GroupInfoViewModel com_nonsense_chat_ui_group_GroupInfoViewModel2;

      @KeepFieldType
      AuthViewModel com_nonsense_chat_ui_auth_AuthViewModel2;

      @KeepFieldType
      ProfileViewModel com_nonsense_chat_ui_profile_ProfileViewModel2;

      @KeepFieldType
      StickerPickerViewModel com_nonsense_chat_ui_chat_StickerPickerViewModel2;
    }
  }

  private static final class ViewModelCImpl extends NonsenseApp_HiltComponents.ViewModelC {
    private final SavedStateHandle savedStateHandle;

    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    private Provider<AuthViewModel> authViewModelProvider;

    private Provider<CallViewModel> callViewModelProvider;

    private Provider<ChatListViewModel> chatListViewModelProvider;

    private Provider<ChatViewModel> chatViewModelProvider;

    private Provider<EditProfileViewModel> editProfileViewModelProvider;

    private Provider<FriendsViewModel> friendsViewModelProvider;

    private Provider<GroupInfoViewModel> groupInfoViewModelProvider;

    private Provider<NewGroupViewModel> newGroupViewModelProvider;

    private Provider<ProfileViewModel> profileViewModelProvider;

    private Provider<RootViewModel> rootViewModelProvider;

    private Provider<SettingsViewModel> settingsViewModelProvider;

    private Provider<StickerPackViewModel> stickerPackViewModelProvider;

    private Provider<StickerPickerViewModel> stickerPickerViewModelProvider;

    private Provider<VpnViewModel> vpnViewModelProvider;

    private ViewModelCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, SavedStateHandle savedStateHandleParam,
        ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.savedStateHandle = savedStateHandleParam;
      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.authViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.callViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.chatListViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.chatViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.editProfileViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
      this.friendsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 5);
      this.groupInfoViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 6);
      this.newGroupViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 7);
      this.profileViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 8);
      this.rootViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 9);
      this.settingsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 10);
      this.stickerPackViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 11);
      this.stickerPickerViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 12);
      this.vpnViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 13);
    }

    @Override
    public Map<Class<?>, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return LazyClassKeyMap.<javax.inject.Provider<ViewModel>>of(MapBuilder.<String, javax.inject.Provider<ViewModel>>newMapBuilder(14).put(LazyClassKeyProvider.com_nonsense_chat_ui_auth_AuthViewModel, ((Provider) authViewModelProvider)).put(LazyClassKeyProvider.com_nonsense_chat_call_CallViewModel, ((Provider) callViewModelProvider)).put(LazyClassKeyProvider.com_nonsense_chat_ui_chatlist_ChatListViewModel, ((Provider) chatListViewModelProvider)).put(LazyClassKeyProvider.com_nonsense_chat_ui_chat_ChatViewModel, ((Provider) chatViewModelProvider)).put(LazyClassKeyProvider.com_nonsense_chat_ui_profile_EditProfileViewModel, ((Provider) editProfileViewModelProvider)).put(LazyClassKeyProvider.com_nonsense_chat_ui_friends_FriendsViewModel, ((Provider) friendsViewModelProvider)).put(LazyClassKeyProvider.com_nonsense_chat_ui_group_GroupInfoViewModel, ((Provider) groupInfoViewModelProvider)).put(LazyClassKeyProvider.com_nonsense_chat_ui_group_NewGroupViewModel, ((Provider) newGroupViewModelProvider)).put(LazyClassKeyProvider.com_nonsense_chat_ui_profile_ProfileViewModel, ((Provider) profileViewModelProvider)).put(LazyClassKeyProvider.com_nonsense_chat_ui_navigation_RootViewModel, ((Provider) rootViewModelProvider)).put(LazyClassKeyProvider.com_nonsense_chat_ui_settings_SettingsViewModel, ((Provider) settingsViewModelProvider)).put(LazyClassKeyProvider.com_nonsense_chat_ui_chat_StickerPackViewModel, ((Provider) stickerPackViewModelProvider)).put(LazyClassKeyProvider.com_nonsense_chat_ui_chat_StickerPickerViewModel, ((Provider) stickerPickerViewModelProvider)).put(LazyClassKeyProvider.com_nonsense_chat_ui_vpn_VpnViewModel, ((Provider) vpnViewModelProvider)).build());
    }

    @Override
    public Map<Class<?>, Object> getHiltViewModelAssistedMap() {
      return Collections.<Class<?>, Object>emptyMap();
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_nonsense_chat_ui_auth_AuthViewModel = "com.nonsense.chat.ui.auth.AuthViewModel";

      static String com_nonsense_chat_ui_chat_StickerPackViewModel = "com.nonsense.chat.ui.chat.StickerPackViewModel";

      static String com_nonsense_chat_ui_chat_StickerPickerViewModel = "com.nonsense.chat.ui.chat.StickerPickerViewModel";

      static String com_nonsense_chat_ui_profile_EditProfileViewModel = "com.nonsense.chat.ui.profile.EditProfileViewModel";

      static String com_nonsense_chat_ui_chatlist_ChatListViewModel = "com.nonsense.chat.ui.chatlist.ChatListViewModel";

      static String com_nonsense_chat_ui_chat_ChatViewModel = "com.nonsense.chat.ui.chat.ChatViewModel";

      static String com_nonsense_chat_call_CallViewModel = "com.nonsense.chat.call.CallViewModel";

      static String com_nonsense_chat_ui_vpn_VpnViewModel = "com.nonsense.chat.ui.vpn.VpnViewModel";

      static String com_nonsense_chat_ui_group_GroupInfoViewModel = "com.nonsense.chat.ui.group.GroupInfoViewModel";

      static String com_nonsense_chat_ui_group_NewGroupViewModel = "com.nonsense.chat.ui.group.NewGroupViewModel";

      static String com_nonsense_chat_ui_profile_ProfileViewModel = "com.nonsense.chat.ui.profile.ProfileViewModel";

      static String com_nonsense_chat_ui_navigation_RootViewModel = "com.nonsense.chat.ui.navigation.RootViewModel";

      static String com_nonsense_chat_ui_friends_FriendsViewModel = "com.nonsense.chat.ui.friends.FriendsViewModel";

      static String com_nonsense_chat_ui_settings_SettingsViewModel = "com.nonsense.chat.ui.settings.SettingsViewModel";

      @KeepFieldType
      AuthViewModel com_nonsense_chat_ui_auth_AuthViewModel2;

      @KeepFieldType
      StickerPackViewModel com_nonsense_chat_ui_chat_StickerPackViewModel2;

      @KeepFieldType
      StickerPickerViewModel com_nonsense_chat_ui_chat_StickerPickerViewModel2;

      @KeepFieldType
      EditProfileViewModel com_nonsense_chat_ui_profile_EditProfileViewModel2;

      @KeepFieldType
      ChatListViewModel com_nonsense_chat_ui_chatlist_ChatListViewModel2;

      @KeepFieldType
      ChatViewModel com_nonsense_chat_ui_chat_ChatViewModel2;

      @KeepFieldType
      CallViewModel com_nonsense_chat_call_CallViewModel2;

      @KeepFieldType
      VpnViewModel com_nonsense_chat_ui_vpn_VpnViewModel2;

      @KeepFieldType
      GroupInfoViewModel com_nonsense_chat_ui_group_GroupInfoViewModel2;

      @KeepFieldType
      NewGroupViewModel com_nonsense_chat_ui_group_NewGroupViewModel2;

      @KeepFieldType
      ProfileViewModel com_nonsense_chat_ui_profile_ProfileViewModel2;

      @KeepFieldType
      RootViewModel com_nonsense_chat_ui_navigation_RootViewModel2;

      @KeepFieldType
      FriendsViewModel com_nonsense_chat_ui_friends_FriendsViewModel2;

      @KeepFieldType
      SettingsViewModel com_nonsense_chat_ui_settings_SettingsViewModel2;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.nonsense.chat.ui.auth.AuthViewModel 
          return (T) new AuthViewModel(singletonCImpl.authRepositoryProvider.get());

          case 1: // com.nonsense.chat.call.CallViewModel 
          return (T) new CallViewModel(singletonCImpl.callManagerProvider.get(), singletonCImpl.userRepositoryProvider.get(), singletonCImpl.accountManagerProvider.get());

          case 2: // com.nonsense.chat.ui.chatlist.ChatListViewModel 
          return (T) new ChatListViewModel(singletonCImpl.accountManagerProvider.get(), singletonCImpl.chatRepositoryProvider.get(), singletonCImpl.folderRepositoryProvider.get(), singletonCImpl.userRepositoryProvider.get(), singletonCImpl.friendRepositoryProvider.get(), singletonCImpl.realtimeBusProvider.get(), singletonCImpl.settingsStoreProvider.get(), singletonCImpl.connectionMonitorProvider.get());

          case 3: // com.nonsense.chat.ui.chat.ChatViewModel 
          return (T) new ChatViewModel(viewModelCImpl.savedStateHandle, singletonCImpl.accountManagerProvider.get(), singletonCImpl.chatRepositoryProvider.get(), singletonCImpl.messageRepositoryProvider.get(), singletonCImpl.userRepositoryProvider.get(), singletonCImpl.storageRepositoryProvider.get(), singletonCImpl.presenceRepositoryProvider.get(), singletonCImpl.callRepositoryProvider.get(), singletonCImpl.callManagerProvider.get(), singletonCImpl.realtimeBusProvider.get());

          case 4: // com.nonsense.chat.ui.profile.EditProfileViewModel 
          return (T) new EditProfileViewModel(singletonCImpl.accountManagerProvider.get(), singletonCImpl.userRepositoryProvider.get(), singletonCImpl.storageRepositoryProvider.get());

          case 5: // com.nonsense.chat.ui.friends.FriendsViewModel 
          return (T) new FriendsViewModel(singletonCImpl.accountManagerProvider.get(), singletonCImpl.userRepositoryProvider.get(), singletonCImpl.friendRepositoryProvider.get(), singletonCImpl.chatRepositoryProvider.get());

          case 6: // com.nonsense.chat.ui.group.GroupInfoViewModel 
          return (T) new GroupInfoViewModel(viewModelCImpl.savedStateHandle, singletonCImpl.accountManagerProvider.get(), singletonCImpl.chatRepositoryProvider.get(), singletonCImpl.userRepositoryProvider.get(), singletonCImpl.messageRepositoryProvider.get(), singletonCImpl.storageRepositoryProvider.get());

          case 7: // com.nonsense.chat.ui.group.NewGroupViewModel 
          return (T) new NewGroupViewModel(singletonCImpl.accountManagerProvider.get(), singletonCImpl.userRepositoryProvider.get(), singletonCImpl.chatRepositoryProvider.get());

          case 8: // com.nonsense.chat.ui.profile.ProfileViewModel 
          return (T) new ProfileViewModel(viewModelCImpl.savedStateHandle, singletonCImpl.accountManagerProvider.get(), singletonCImpl.userRepositoryProvider.get(), singletonCImpl.chatRepositoryProvider.get(), singletonCImpl.messageRepositoryProvider.get());

          case 9: // com.nonsense.chat.ui.navigation.RootViewModel 
          return (T) new RootViewModel(singletonCImpl.accountManagerProvider.get());

          case 10: // com.nonsense.chat.ui.settings.SettingsViewModel 
          return (T) new SettingsViewModel(singletonCImpl.accountManagerProvider.get(), singletonCImpl.settingsStoreProvider.get(), singletonCImpl.userRepositoryProvider.get());

          case 11: // com.nonsense.chat.ui.chat.StickerPackViewModel 
          return (T) new StickerPackViewModel(singletonCImpl.stickerRepositoryProvider.get(), singletonCImpl.accountManagerProvider.get());

          case 12: // com.nonsense.chat.ui.chat.StickerPickerViewModel 
          return (T) new StickerPickerViewModel(singletonCImpl.stickerRepositoryProvider.get(), singletonCImpl.storageRepositoryProvider.get(), singletonCImpl.accountManagerProvider.get());

          case 13: // com.nonsense.chat.ui.vpn.VpnViewModel 
          return (T) new VpnViewModel(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.settingsStoreProvider.get(), singletonCImpl.vpnStateRepositoryProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends NonsenseApp_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    private Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    private ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle 
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends NonsenseApp_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    private ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }

    private SingBoxConfigBuilder singBoxConfigBuilder() {
      return new SingBoxConfigBuilder(singletonCImpl.provideJsonProvider.get());
    }

    @Override
    public void injectNonsenseVpnService(NonsenseVpnService nonsenseVpnService) {
      injectNonsenseVpnService2(nonsenseVpnService);
    }

    @Override
    public void injectFcmService(FcmService fcmService) {
      injectFcmService2(fcmService);
    }

    @CanIgnoreReturnValue
    private NonsenseVpnService injectNonsenseVpnService2(NonsenseVpnService instance) {
      NonsenseVpnService_MembersInjector.injectSettings(instance, singletonCImpl.settingsStoreProvider.get());
      NonsenseVpnService_MembersInjector.injectStates(instance, singletonCImpl.vpnStateRepositoryProvider.get());
      NonsenseVpnService_MembersInjector.injectConfigBuilder(instance, singBoxConfigBuilder());
      NonsenseVpnService_MembersInjector.injectNotification(instance, singletonCImpl.vpnNotificationProvider.get());
      NonsenseVpnService_MembersInjector.injectEngine(instance, singletonCImpl.provideTunnelEngineProvider.get());
      return instance;
    }

    @CanIgnoreReturnValue
    private FcmService injectFcmService2(FcmService instance2) {
      FcmService_MembersInjector.injectPushTokens(instance2, singletonCImpl.pushTokenManagerProvider.get());
      FcmService_MembersInjector.injectAuth(instance2, singletonCImpl.authRepositoryProvider.get());
      FcmService_MembersInjector.injectSettings(instance2, singletonCImpl.settingsStoreProvider.get());
      return instance2;
    }
  }

  private static final class SingletonCImpl extends NonsenseApp_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    private Provider<SettingsStore> settingsStoreProvider;

    private Provider<CoroutineScope> provideAppScopeProvider;

    private Provider<ProxyController> proxyControllerProvider;

    private Provider<ImageLoader> provideImageLoaderProvider;

    private Provider<SupabaseClient> provideSupabaseProvider;

    private Provider<ConnectionMonitor> connectionMonitorProvider;

    private Provider<DocRepository> docRepositoryProvider;

    private Provider<AuthRepository> authRepositoryProvider;

    private Provider<RealtimeBus> realtimeBusProvider;

    private Provider<AppDatabase> provideAppDatabaseProvider;

    private Provider<DocCache> docCacheProvider;

    private Provider<UserRepository> userRepositoryProvider;

    private Provider<PresenceRepository> presenceRepositoryProvider;

    private Provider<PushTokenManager> pushTokenManagerProvider;

    private Provider<AccountManager> accountManagerProvider;

    private Provider<CallRepository> callRepositoryProvider;

    private Provider<CallManager> callManagerProvider;

    private Provider<ChatRepository> chatRepositoryProvider;

    private Provider<FolderRepository> folderRepositoryProvider;

    private Provider<FriendRepository> friendRepositoryProvider;

    private Provider<MessageRepository> messageRepositoryProvider;

    private Provider<StorageRepository> storageRepositoryProvider;

    private Provider<StickerRepository> stickerRepositoryProvider;

    private Provider<VpnStateRepository> vpnStateRepositoryProvider;

    private Provider<Json> provideJsonProvider;

    private Provider<VpnNotification> vpnNotificationProvider;

    private Provider<TunnelEngine> provideTunnelEngineProvider;

    private SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);
      initialize2(applicationContextModuleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.settingsStoreProvider = DoubleCheck.provider(new SwitchingProvider<SettingsStore>(singletonCImpl, 2));
      this.provideAppScopeProvider = DoubleCheck.provider(new SwitchingProvider<CoroutineScope>(singletonCImpl, 3));
      this.proxyControllerProvider = DoubleCheck.provider(new SwitchingProvider<ProxyController>(singletonCImpl, 1));
      this.provideImageLoaderProvider = DoubleCheck.provider(new SwitchingProvider<ImageLoader>(singletonCImpl, 0));
      this.provideSupabaseProvider = DoubleCheck.provider(new SwitchingProvider<SupabaseClient>(singletonCImpl, 6));
      this.connectionMonitorProvider = DoubleCheck.provider(new SwitchingProvider<ConnectionMonitor>(singletonCImpl, 8));
      this.docRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<DocRepository>(singletonCImpl, 7));
      this.authRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<AuthRepository>(singletonCImpl, 5));
      this.realtimeBusProvider = DoubleCheck.provider(new SwitchingProvider<RealtimeBus>(singletonCImpl, 10));
      this.provideAppDatabaseProvider = DoubleCheck.provider(new SwitchingProvider<AppDatabase>(singletonCImpl, 12));
      this.docCacheProvider = DoubleCheck.provider(new SwitchingProvider<DocCache>(singletonCImpl, 11));
      this.userRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<UserRepository>(singletonCImpl, 9));
      this.presenceRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<PresenceRepository>(singletonCImpl, 13));
      this.pushTokenManagerProvider = DoubleCheck.provider(new SwitchingProvider<PushTokenManager>(singletonCImpl, 14));
      this.accountManagerProvider = DoubleCheck.provider(new SwitchingProvider<AccountManager>(singletonCImpl, 4));
      this.callRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<CallRepository>(singletonCImpl, 16));
      this.callManagerProvider = DoubleCheck.provider(new SwitchingProvider<CallManager>(singletonCImpl, 15));
      this.chatRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<ChatRepository>(singletonCImpl, 17));
      this.folderRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<FolderRepository>(singletonCImpl, 18));
      this.friendRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<FriendRepository>(singletonCImpl, 19));
      this.messageRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<MessageRepository>(singletonCImpl, 20));
      this.storageRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<StorageRepository>(singletonCImpl, 21));
      this.stickerRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<StickerRepository>(singletonCImpl, 22));
      this.vpnStateRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<VpnStateRepository>(singletonCImpl, 23));
      this.provideJsonProvider = DoubleCheck.provider(new SwitchingProvider<Json>(singletonCImpl, 24));
    }

    @SuppressWarnings("unchecked")
    private void initialize2(final ApplicationContextModule applicationContextModuleParam) {
      this.vpnNotificationProvider = DoubleCheck.provider(new SwitchingProvider<VpnNotification>(singletonCImpl, 25));
      this.provideTunnelEngineProvider = DoubleCheck.provider(new SwitchingProvider<TunnelEngine>(singletonCImpl, 26));
    }

    @Override
    public void injectNonsenseApp(NonsenseApp nonsenseApp) {
      injectNonsenseApp2(nonsenseApp);
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return Collections.<Boolean>emptySet();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    @CanIgnoreReturnValue
    private NonsenseApp injectNonsenseApp2(NonsenseApp instance) {
      NonsenseApp_MembersInjector.injectImageLoader(instance, provideImageLoaderProvider.get());
      NonsenseApp_MembersInjector.injectAccount(instance, accountManagerProvider.get());
      return instance;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // coil.ImageLoader 
          return (T) AppModule_ProvideImageLoaderFactory.provideImageLoader(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.proxyControllerProvider.get());

          case 1: // com.nonsense.chat.data.proxy.ProxyController 
          return (T) new ProxyController(singletonCImpl.settingsStoreProvider.get(), singletonCImpl.provideAppScopeProvider.get());

          case 2: // com.nonsense.chat.data.SettingsStore 
          return (T) new SettingsStore(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 3: // @com.nonsense.chat.di.AppScope kotlinx.coroutines.CoroutineScope 
          return (T) AppModule_ProvideAppScopeFactory.provideAppScope();

          case 4: // com.nonsense.chat.data.AccountManager 
          return (T) new AccountManager(singletonCImpl.authRepositoryProvider.get(), singletonCImpl.userRepositoryProvider.get(), singletonCImpl.presenceRepositoryProvider.get(), singletonCImpl.pushTokenManagerProvider.get(), singletonCImpl.provideAppScopeProvider.get());

          case 5: // com.nonsense.chat.data.repos.AuthRepository 
          return (T) new AuthRepository(singletonCImpl.provideSupabaseProvider.get(), singletonCImpl.docRepositoryProvider.get());

          case 6: // io.github.jan.supabase.SupabaseClient 
          return (T) AppModule_ProvideSupabaseFactory.provideSupabase(singletonCImpl.proxyControllerProvider.get());

          case 7: // com.nonsense.chat.data.DocRepository 
          return (T) new DocRepository(singletonCImpl.provideSupabaseProvider.get(), singletonCImpl.connectionMonitorProvider.get());

          case 8: // com.nonsense.chat.data.ConnectionMonitor 
          return (T) new ConnectionMonitor();

          case 9: // com.nonsense.chat.data.repos.UserRepository 
          return (T) new UserRepository(singletonCImpl.provideSupabaseProvider.get(), singletonCImpl.docRepositoryProvider.get(), singletonCImpl.realtimeBusProvider.get(), singletonCImpl.docCacheProvider.get());

          case 10: // com.nonsense.chat.data.RealtimeBus 
          return (T) new RealtimeBus(singletonCImpl.provideSupabaseProvider.get(), singletonCImpl.provideAppScopeProvider.get());

          case 11: // com.nonsense.chat.data.cache.DocCache 
          return (T) new DocCache(singletonCImpl.provideAppDatabaseProvider.get());

          case 12: // com.nonsense.chat.data.cache.AppDatabase 
          return (T) AppModule_ProvideAppDatabaseFactory.provideAppDatabase(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 13: // com.nonsense.chat.data.repos.PresenceRepository 
          return (T) new PresenceRepository(singletonCImpl.docRepositoryProvider.get());

          case 14: // com.nonsense.chat.push.PushTokenManager 
          return (T) new PushTokenManager(singletonCImpl.docRepositoryProvider.get());

          case 15: // com.nonsense.chat.call.CallManager 
          return (T) new CallManager(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.callRepositoryProvider.get(), singletonCImpl.accountManagerProvider.get(), singletonCImpl.provideAppScopeProvider.get());

          case 16: // com.nonsense.chat.data.repos.CallRepository 
          return (T) new CallRepository(singletonCImpl.docRepositoryProvider.get(), singletonCImpl.realtimeBusProvider.get());

          case 17: // com.nonsense.chat.data.repos.ChatRepository 
          return (T) new ChatRepository(singletonCImpl.docRepositoryProvider.get(), singletonCImpl.realtimeBusProvider.get(), singletonCImpl.docCacheProvider.get());

          case 18: // com.nonsense.chat.data.repos.FolderRepository 
          return (T) new FolderRepository(singletonCImpl.docRepositoryProvider.get(), singletonCImpl.realtimeBusProvider.get());

          case 19: // com.nonsense.chat.data.repos.FriendRepository 
          return (T) new FriendRepository(singletonCImpl.docRepositoryProvider.get(), singletonCImpl.realtimeBusProvider.get());

          case 20: // com.nonsense.chat.data.repos.MessageRepository 
          return (T) new MessageRepository(singletonCImpl.docRepositoryProvider.get(), singletonCImpl.realtimeBusProvider.get(), singletonCImpl.docCacheProvider.get());

          case 21: // com.nonsense.chat.data.StorageRepository 
          return (T) new StorageRepository(singletonCImpl.provideSupabaseProvider.get(), ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 22: // com.nonsense.chat.data.repos.StickerRepository 
          return (T) new StickerRepository(singletonCImpl.docRepositoryProvider.get());

          case 23: // com.nonsense.chat.data.vpn.VpnStateRepository 
          return (T) new VpnStateRepository();

          case 24: // kotlinx.serialization.json.Json 
          return (T) AppModule_ProvideJsonFactory.provideJson();

          case 25: // com.nonsense.chat.data.vpn.VpnNotification 
          return (T) new VpnNotification();

          case 26: // com.nonsense.chat.data.vpn.TunnelEngine 
          return (T) AppModule_ProvideTunnelEngineFactory.provideTunnelEngine(new NoopTunnelEngine());

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
