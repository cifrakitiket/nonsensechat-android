package com.nonsense.chat.ui.friends;

import com.nonsense.chat.data.AccountManager;
import com.nonsense.chat.data.repos.ChatRepository;
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
public final class FriendsViewModel_Factory implements Factory<FriendsViewModel> {
  private final Provider<AccountManager> accountProvider;

  private final Provider<UserRepository> usersProvider;

  private final Provider<FriendRepository> friendsProvider;

  private final Provider<ChatRepository> chatsProvider;

  public FriendsViewModel_Factory(Provider<AccountManager> accountProvider,
      Provider<UserRepository> usersProvider, Provider<FriendRepository> friendsProvider,
      Provider<ChatRepository> chatsProvider) {
    this.accountProvider = accountProvider;
    this.usersProvider = usersProvider;
    this.friendsProvider = friendsProvider;
    this.chatsProvider = chatsProvider;
  }

  @Override
  public FriendsViewModel get() {
    return newInstance(accountProvider.get(), usersProvider.get(), friendsProvider.get(), chatsProvider.get());
  }

  public static FriendsViewModel_Factory create(Provider<AccountManager> accountProvider,
      Provider<UserRepository> usersProvider, Provider<FriendRepository> friendsProvider,
      Provider<ChatRepository> chatsProvider) {
    return new FriendsViewModel_Factory(accountProvider, usersProvider, friendsProvider, chatsProvider);
  }

  public static FriendsViewModel newInstance(AccountManager account, UserRepository users,
      FriendRepository friends, ChatRepository chats) {
    return new FriendsViewModel(account, users, friends, chats);
  }
}
