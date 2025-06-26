package com.example.vintageradioapp.data.di

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

	@Provides
	@Singleton
	fun provideContext(
		app: Application
	): Context {
		return app.applicationContext
	}
}