package com.nonsense.chat.ui.group;

import com.nonsense.chat.data.AccountManager;
import com.nonsense.chat.data.repos.ChatRepository;
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
public final class NewGroupViewModel_Factory implements Factory<NewGroupViewModel> {
  private final Provider<AccountManager> accountProvider;

  private final Provider<UserRepository> usersProvider;

  private final Provider<ChatRepository> chatsProvider;

  public NewGroupViewModel_Factory(Provider<AccountManager> accountProvider,
      Provider<UserRepository> usersProvider, Provider<ChatRepository> chatsProvider) {
    this.accountProvider = accountProvider;
    this.usersProvider = usersProvider;
    this.chatsProvider = chatsProvider;
  }

  @Override
  public NewGroupViewModel get() {
    return newInstance(accountProvider.get(), usersProvider.get(), chatsProvider.get());
  }

  public static NewGroupViewModel_Factory create(Provider<AccountManager> accountProvider,
      Provider<UserRepository> usersProvider, Provider<ChatRepository> chatsProvider) {
    return new NewGroupViewModel_Factory(accountProvider, usersProvider, chatsProvider);
  }

  public static NewGroupViewModel newInstance(AccountManager account, UserRepository users,
      ChatRepository chats) {
    return new NewGroupViewModel(account, users, chats);
  }
}
