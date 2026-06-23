package com.nonsense.chat.ui.chat;

import com.nonsense.chat.data.AccountManager;
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
public final class StickerPackViewModel_Factory implements Factory<StickerPackViewModel> {
  private final Provider<StickerRepository> stickersProvider;

  private final Provider<AccountManager> accountProvider;

  public StickerPackViewModel_Factory(Provider<StickerRepository> stickersProvider,
      Provider<AccountManager> accountProvider) {
    this.stickersProvider = stickersProvider;
    this.accountProvider = accountProvider;
  }

  @Override
  public StickerPackViewModel get() {
    return newInstance(stickersProvider.get(), accountProvider.get());
  }

  public static StickerPackViewModel_Factory create(Provider<StickerRepository> stickersProvider,
      Provider<AccountManager> accountProvider) {
    return new StickerPackViewModel_Factory(stickersProvider, accountProvider);
  }

  public static StickerPackViewModel newInstance(StickerRepository stickers,
      AccountManager account) {
    return new StickerPackViewModel(stickers, account);
  }
}
