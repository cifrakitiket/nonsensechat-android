package com.nonsense.chat.ui.chat;

import androidx.lifecycle.SavedStateHandle;
import com.nonsense.chat.call.CallManager;
import com.nonsense.chat.data.AccountManager;
import com.nonsense.chat.data.RealtimeBus;
import com.nonsense.chat.data.StorageRepository;
import com.nonsense.chat.data.repos.CallRepository;
import com.nonsense.chat.data.repos.ChatRepository;
import com.nonsense.chat.data.repos.MessageRepository;
import com.nonsense.chat.data.repos.PresenceRepository;
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
public final class ChatViewModel_Factory implements Factory<ChatViewModel> {
  private final Provider<SavedStateHandle> savedStateProvider;

  private final Provider<AccountManager> accountProvider;

  private final Provider<ChatRepository> chatsProvider;

  private final Provider<MessageRepository> messagesProvider;

  private final Provider<UserRepository> usersProvider;

  private final Provider<StorageRepository> storageProvider;

  private final Provider<PresenceRepository> presenceProvider;

  private final Provider<CallRepository> callsProvider;

  private final Provider<CallManager> callManagerProvider;

  private final Provider<RealtimeBus> realtimeProvider;

  public ChatViewModel_Factory(Provider<SavedStateHandle> savedStateProvider,
      Provider<AccountManager> accountProvider, Provider<ChatRepository> chatsProvider,
      Provider<MessageRepository> messagesProvider, Provider<UserRepository> usersProvider,
      Provider<StorageRepository> storageProvider, Provider<PresenceRepository> presenceProvider,
      Provider<CallRepository> callsProvider, Provider<CallManager> callManagerProvider,
      Provider<RealtimeBus> realtimeProvider) {
    this.savedStateProvider = savedStateProvider;
    this.accountProvider = accountProvider;
    this.chatsProvider = chatsProvider;
    this.messagesProvider = messagesProvider;
    this.usersProvider = usersProvider;
    this.storageProvider = storageProvider;
    this.presenceProvider = presenceProvider;
    this.callsProvider = callsProvider;
    this.callManagerProvider = callManagerProvider;
    this.realtimeProvider = realtimeProvider;
  }

  @Override
  public ChatViewModel get() {
    return newInstance(savedStateProvider.get(), accountProvider.get(), chatsProvider.get(), messagesProvider.get(), usersProvider.get(), storageProvider.get(), presenceProvider.get(), callsProvider.get(), callManagerProvider.get(), realtimeProvider.get());
  }

  public static ChatViewModel_Factory create(Provider<SavedStateHandle> savedStateProvider,
      Provider<AccountManager> accountProvider, Provider<ChatRepository> chatsProvider,
      Provider<MessageRepository> messagesProvider, Provider<UserRepository> usersProvider,
      Provider<StorageRepository> storageProvider, Provider<PresenceRepository> presenceProvider,
      Provider<CallRepository> callsProvider, Provider<CallManager> callManagerProvider,
      Provider<RealtimeBus> realtimeProvider) {
    return new ChatViewModel_Factory(savedStateProvider, accountProvider, chatsProvider, messagesProvider, usersProvider, storageProvider, presenceProvider, callsProvider, callManagerProvider, realtimeProvider);
  }

  public static ChatViewModel newInstance(SavedStateHandle savedState, AccountManager account,
      ChatRepository chats, MessageRepository messages, UserRepository users,
      StorageRepository storage, PresenceRepository presence, CallRepository calls,
      CallManager callManager, RealtimeBus realtime) {
    return new ChatViewModel(savedState, account, chats, messages, users, storage, presence, calls, callManager, realtime);
  }
}
