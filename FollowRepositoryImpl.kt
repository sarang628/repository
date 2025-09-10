package com.sarang.torang.di.repository

import com.google.gson.JsonSyntaxException
import com.sarang.torang.api.ApiProfile
import com.sarang.torang.api.handle
import com.sarang.torang.data.remote.response.FollowerApiModel
import com.sarang.torang.repository.FollowRepository
import com.sarang.torang.session.SessionService
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FollowRepositoryImpl @Inject constructor(
    val apiProfile: ApiProfile,
    val sessionService: SessionService
) : FollowRepository {
    override suspend fun getMyFollower(): List<FollowerApiModel> {
        try {
            sessionService.getToken()?.let {
                return apiProfile.getMyFollower(it)
            }
        } catch (e: HttpException) {
            throw Exception(e.handle())
        } catch (e: JsonSyntaxException) {
            throw Exception(e.toString())
        }
        return ArrayList()
    }

    override suspend fun getFollower(userId: Int): List<FollowerApiModel> {
        try {
            return apiProfile.getFollower(userId)
        } catch (e: HttpException) {
            throw Exception(e.handle())
        } catch (e: JsonSyntaxException) {
            throw Exception(e.toString())
        }
        return ArrayList()
    }

    override suspend fun getMyFollowing(): List<FollowerApiModel> {
        try {
            sessionService.getToken()?.let {
                return apiProfile.getMyFollowing(it)
            }
        } catch (e: HttpException) {
            throw Exception(e.handle())
        } catch (e: JsonSyntaxException) {
            throw Exception(e.toString())
        }
        return ArrayList()
    }

    override suspend fun getFollowing(userId: Int): List<FollowerApiModel> {
        try {
            return apiProfile.getFollowing(userId)
        } catch (e: HttpException) {
            throw Exception(e.handle())
        } catch (e: JsonSyntaxException) {
            throw Exception(e.toString())
        }
        return ArrayList()
    }

    override suspend fun follow(userId: Int): Boolean {
        try {
            sessionService.getToken()?.let {
                return apiProfile.follow(it, userId)
            }
        } catch (e: HttpException) {
            throw Exception(e.handle())
        } catch (e: JsonSyntaxException) {
            throw Exception(e.toString())
        }

        throw Exception("알 수 없는 오류가 발생했습니다.")
    }

    override suspend fun unFollow(userId: Int): Boolean {
        try {
            sessionService.getToken()?.let {
                return apiProfile.unfollow(it, userId)
            }
        } catch (e: HttpException) {
            throw Exception(e.handle())
        } catch (e: JsonSyntaxException) {
            throw Exception(e.toString())
        }
        throw Exception("알 수 없는 오류가 발생했습니다.")
    }

    override suspend fun delete(userId: Int): Boolean {
        try {
            sessionService.getToken()?.let {
                return apiProfile.delete(it, userId)
            }
        } catch (e: HttpException) {
            throw Exception(e.handle())
        } catch (e: JsonSyntaxException) {
            throw Exception(e.toString())
        }
        throw Exception("알 수 없는 오류가 발생했습니다.")
    }
}