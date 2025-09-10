package com.sarang.torang.di.repository

import com.sarang.torang.session.SessionClientService
import com.sarang.torang.session.SessionService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
abstract class SessionClientServiceModule {
    @Binds
    abstract fun provideClientService(sessionService: SessionService): SessionClientService
}