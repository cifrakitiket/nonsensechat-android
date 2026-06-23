package com.nonsense.chat.ui.group;

import androidx.lifecycle.SavedStateHandle;
import com.nonsense.chat.data.AccountManager;
import com.nonsense.chat.data.StorageRepository;
import com.nonsense.chat.data.repos.ChatRepository;
import com.nonsense.chat.data.repos.MessageRepository;
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
public final class GroupInfoViewModel_Factory implements Factory<GroupInfoViewModel> {
  private final Provider<SavedStateHandle> savedStateProvider;

  private final Provider<AccountManager> accountProvider;

  private final Provider<ChatRepository> chatsProvider;

  private final Provider<UserRepository> usersProvider;

  private final Provider<MessageRepository> messagesProvider;

  private final Provider<StorageRepository> storageProvider;

  public GroupInfoViewModel_Factory(Provider<SavedStateHandle> savedStateProvider,
      Provider<AccountManager> accountProvider, Provider<ChatRepository> chatsProvider,
      Provider<UserRepository> usersProvider, Provider<MessageRepository> messagesProvider,
      Provider<StorageRepository> storageProvider) {
    this.savedStateProvider = savedStateProvider;
    this.accountProvider = accountProvider;
    this.chatsProvider = chatsProvider;
    this.usersProvider = usersProvider;
    this.messagesProvider = messagesProvider;
    this.storageProvider = storageProvider;
  }

  @Override
  public GroupInfoViewModel get() {
    return newInstance(savedStateProvider.get(), accountProvider.get(), chatsProvider.get(), usersProvider.get(), messagesProvider.get(), storageProvider.get());
  }

  public static GroupInfoViewModel_Factory create(Provider<SavedStateHandle> savedStateProvider,
      Provider<AccountManager> accountProvider, Provider<ChatRepository> chatsProvider,
      Provider<UserRepository> usersProvider, Provider<MessageRepository> messagesProvider,
      Provider<StorageRepository> storageProvider) {
    return new GroupInfoViewModel_Factory(savedStateProvider, accountProvider, chatsProvider, usersProvider, messagesProvider, storageProvider);
  }

  public static GroupInfoViewModel newInstance(SavedStateHandle savedState, AccountManager account,
      ChatRepository chats, UserRepository users, MessageRepository messages,
      StorageRepository storage) {
    return new GroupInfoViewModel(savedState, account, chats, users, messages, storage);
  }
}
