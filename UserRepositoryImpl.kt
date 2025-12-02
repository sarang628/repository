package com.sarang.torang.di.repository

import com.sarang.torang.api.ApiProfile
import com.sarang.torang.api.handle
import com.sarang.torang.data.remote.response.UserApiModel
import com.sarang.torang.repository.UserRepository
import com.sarang.torang.session.SessionClientService
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class UserRepositoryImpl @Inject constructor(
    private val apiProfile: ApiProfile,
    private val sessionClientService: SessionClientService,
) : UserRepository {

    override suspend fun findById(userId: Int): UserApiModel {
        return apiProfile.getProfile("$userId")
    }

    override suspend fun findByToken(): UserApiModel {
        sessionClientService.getToken()?.let {
            try {
                return apiProfile.getProfileByToken(it)
            } catch (e: HttpException) {
                throw Exception(e.handle())
            }
        }
        throw Exception("로그인해 주세요.")
    }
}