package com.sryang.torang_repository.di.repository.service

import com.sryang.torang_repository.session.SessionClientService
import com.sryang.torang_repository.session.SessionService
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class SessionClientServiceModule {
    @Binds
    abstract fun provideClientService(sessionService: SessionService): SessionClientService
}