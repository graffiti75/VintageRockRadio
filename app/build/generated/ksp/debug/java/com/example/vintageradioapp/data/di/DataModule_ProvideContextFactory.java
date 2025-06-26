package com.example.vintageradioapp.data.di;

import android.app.Application;
import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
    "KotlinInternalInJava"
})
public final class DataModule_ProvideContextFactory implements Factory<Context> {
  private final Provider<Application> appProvider;

  public DataModule_ProvideContextFactory(Provider<Application> appProvider) {
    this.appProvider = appProvider;
  }

  @Override
  public Context get() {
    return provideContext(appProvider.get());
  }

  public static DataModule_ProvideContextFactory create(Provider<Application> appProvider) {
    return new DataModule_ProvideContextFactory(appProvider);
  }

  public static Context provideContext(Application app) {
    return Preconditions.checkNotNullFromProvides(DataModule.INSTANCE.provideContext(app));
  }
}
