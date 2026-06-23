package com.nonsense.chat.ui.auth;

import com.nonsense.chat.data.repos.AuthRepository;
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
public final class AuthViewModel_Factory implements Factory<AuthViewModel> {
  private final Provider<AuthRepository> authProvider;

  public AuthViewModel_Factory(Provider<AuthRepository> authProvider) {
    this.authProvider = authProvider;
  }

  @Override
  public AuthViewModel get() {
    return newInstance(authProvider.get());
  }

  public static AuthViewModel_Factory create(Provider<AuthRepository> authProvider) {
    return new AuthViewModel_Factory(authProvider);
  }

  public static AuthViewModel newInstance(AuthRepository auth) {
    return new AuthViewModel(auth);
  }
}
