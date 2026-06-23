package com.nonsense.chat.ui.chat;

import com.nonsense.chat.data.AccountManager;
import com.nonsense.chat.data.StorageRepository;
import com.nonsense.chat.data.repos.StickerRepository;
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
public final class StickerPickerViewModel_Factory implements Factory<StickerPickerViewModel> {
  private final Provider<StickerRepository> stickersProvider;

  private final Provider<StorageRepository> storageProvider;

  private final Provider<AccountManager> accountProvider;

  public StickerPickerViewModel_Factory(Provider<StickerRepository> stickersProvider,
      Provider<StorageRepository> storageProvider, Provider<AccountManager> accountProvider) {
    this.stickersProvider = stickersProvider;
    this.storageProvider = storageProvider;
    this.accountProvider = accountProvider;
  }

  @Override
  public StickerPickerViewModel get() {
    return newInstance(stickersProvider.get(), storageProvider.get(), accountProvider.get());
  }

  public static StickerPickerViewModel_Factory create(Provider<StickerRepository> stickersProvider,
      Provider<StorageRepository> storageProvider, Provider<AccountManager> accountProvider) {
    return new StickerPickerViewModel_Factory(stickersProvider, storageProvider, accountProvider);
  }

  public static StickerPickerViewModel newInstance(StickerRepository stickers,
      StorageRepository storage, AccountManager account) {
    return new StickerPickerViewModel(stickers, storage, account);
  }
}
