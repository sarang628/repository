package com.sryang.torang_repository.di.repository.repository.impl

import com.sryang.torang_repository.repository.SettingsRepository
import com.sryang.torang_repository.repository.LoginRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    val loginRepository: LoginRepository
) :
    SettingsRepository {
    override suspend fun logout() {
        loginRepository.logout()
    }

    override fun getUsername(): Flow<String> {
        return loginRepository.getUserName()
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class SettingRepositoryModule {
    @Binds
    abstract fun provideSettingRepository(settingsRepositoryImpl: SettingsRepositoryImpl): SettingsRepository
}