package com.nonsense.chat.ui.profile;

import androidx.lifecycle.SavedStateHandle;
import com.nonsense.chat.data.AccountManager;
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
public final class ProfileViewModel_Factory implements Factory<ProfileViewModel> {
  private final Provider<SavedStateHandle> savedStateProvider;

  private final Provider<AccountManager> accountProvider;

  private final Provider<UserRepository> usersProvider;

  private final Provider<ChatRepository> chatsProvider;

  private final Provider<MessageRepository> messagesProvider;

  public ProfileViewModel_Factory(Provider<SavedStateHandle> savedStateProvider,
      Provider<AccountManager> accountProvider, Provider<UserRepository> usersProvider,
      Provider<ChatRepository> chatsProvider, Provider<MessageRepository> messagesProvider) {
    this.savedStateProvider = savedStateProvider;
    this.accountProvider = accountProvider;
    this.usersProvider = usersProvider;
    this.chatsProvider = chatsProvider;
    this.messagesProvider = messagesProvider;
  }

  @Override
  public ProfileViewModel get() {
    return newInstance(savedStateProvider.get(), accountProvider.get(), usersProvider.get(), chatsProvider.get(), messagesProvider.get());
  }

  public static ProfileViewModel_Factory create(Provider<SavedStateHandle> savedStateProvider,
      Provider<AccountManager> accountProvider, Provider<UserRepository> usersProvider,
      Provider<ChatRepository> chatsProvider, Provider<MessageRepository> messagesProvider) {
    return new ProfileViewModel_Factory(savedStateProvider, accountProvider, usersProvider, chatsProvider, messagesProvider);
  }

  public static ProfileViewModel newInstance(SavedStateHandle savedState, AccountManager account,
      UserRepository users, ChatRepository chats, MessageRepository messages) {
    return new ProfileViewModel(savedState, account, users, chats, messages);
  }
}
