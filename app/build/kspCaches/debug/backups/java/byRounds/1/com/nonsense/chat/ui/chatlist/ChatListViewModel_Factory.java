package com.nonsense.chat.ui.chatlist;

import com.nonsense.chat.data.AccountManager;
import com.nonsense.chat.data.ConnectionMonitor;
import com.nonsense.chat.data.RealtimeBus;
import com.nonsense.chat.data.SettingsStore;
import com.nonsense.chat.data.repos.ChatRepository;
import com.nonsense.chat.data.repos.FolderRepository;
import com.nonsense.chat.data.repos.FriendRepository;
import com.nonsense.chat.data.repos.UserRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class ChatListViewModel_Factory implements Factory<ChatListViewModel> {
  private final Provider<AccountManager> accountProvider;

  private final Provider<ChatRepository> chatsProvider;

  private final Provider<FolderRepository> foldersProvider;

  private final Provider<UserRepository> usersProvider;

  private final Provider<FriendRepository> friendsProvider;

  private final Provider<RealtimeBus> realtimeProvider;

  private final Provider<SettingsStore> settingsProvider;

  private final Provider<ConnectionMonitor> connectionMonitorProvider;

  public ChatListViewModel_Factory(Provider<AccountManager> accountProvider,
      Provider<ChatRepository> chatsProvider, Provider<FolderRepository> foldersProvider,
      Provider<UserRepository> usersProvider, Provider<FriendRepository> friendsProvider,
      Provider<RealtimeBus> realtimeProvider, Provider<SettingsStore> settingsProvider,
      Provider<ConnectionMonitor> connectionMonitorProvider) {
    this.accountProvider = accountProvider;
    this.chatsProvider = chatsProvider;
    this.foldersProvider = foldersProvider;
    this.usersProvider = usersProvider;
    this.friendsProvider = friendsProvider;
    this.realtimeProvider = realtimeProvider;
    this.settingsProvider = settingsProvider;
    this.connectionMonitorProvider = connectionMonitorProvider;
  }

  @Override
  public ChatListViewModel get() {
    return newInstance(accountProvider.get(), chatsProvider.get(), foldersProvider.get(), usersProvider.get(), friendsProvider.get(), realtimeProvider.get(), settingsProvider.get(), connectionMonitorProvider.get());
  }

  public static ChatListViewModel_Factory create(Provider<AccountManager> accountProvider,
      Provider<ChatRepository> chatsProvider, Provider<FolderRepository> foldersProvider,
      Provider<UserRepository> usersProvider, Provider<FriendRepository> friendsProvider,
      Provider<RealtimeBus> realtimeProvider, Provider<SettingsStore> settingsProvider,
      Provider<ConnectionMonitor> connectionMonitorProvider) {
    return new ChatListViewModel_Factory(accountProvider, chatsProvider, foldersProvider, usersProvider, friendsProvider, realtimeProvider, settingsProvider, connectionMonitorProvider);
  }

  public static ChatListViewModel newInstance(AccountManager account, ChatRepository chats,
      FolderRepository folders, UserRepository users, FriendRepository friends,
      RealtimeBus realtime, SettingsStore settings, ConnectionMonitor connectionMonitor) {
    return new ChatListViewModel(account, chats, folders, users, friends, realtime, settings, connectionMonitor);
  }
}
