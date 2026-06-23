package com.nonsense.chat.ui.profile;

import com.nonsense.chat.data.AccountManager;
import com.nonsense.chat.data.StorageRepository;
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
public final class EditProfileViewModel_Factory implements Factory<EditProfileViewModel> {
  private final Provider<AccountManager> accountProvider;

  private final Provider<UserRepository> usersProvider;

  private final Provider<StorageRepository> storageProvider;

  public EditProfileViewModel_Factory(Provider<AccountManager> accountProvider,
      Provider<UserRepository> usersProvider, Provider<StorageRepository> storageProvider) {
    this.accountProvider = accountProvider;
    this.usersProvider = usersProvider;
    this.storageProvider = storageProvider;
  }

  @Override
  public EditProfileViewModel get() {
    return newInstance(accountProvider.get(), usersProvider.get(), storageProvider.get());
  }

  public static EditProfileViewModel_Factory create(Provider<AccountManager> accountProvider,
      Provider<UserRepository> usersProvider, Provider<StorageRepository> storageProvider) {
    return new EditProfileViewModel_Factory(accountProvider, usersProvider, storageProvider);
  }

  public static EditProfileViewModel newInstance(AccountManager account, UserRepository users,
      StorageRepository storage) {
    return new EditProfileViewModel(account, users, storage);
  }
}
