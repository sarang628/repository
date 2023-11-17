package com.sryang.torang_repository.di.repository.repository.impl

import com.sryang.torang_repository.api.ApiLogin
import com.sryang.torang_repository.api.handle
import com.sryang.torang_repository.data.dao.LoggedInUserDao
import com.sryang.torang_repository.data.entity.LoggedInUserEntity
import com.sryang.torang_repository.data.remote.response.LoginResponse
import com.sryang.torang_repository.data.remote.response.RemoteUser
import com.sryang.torang_repository.repository.LoginRepository
import com.sryang.torang_repository.session.SessionService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.net.ConnectException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginRepositoryImpl @Inject constructor(
    private val apiLogin: ApiLogin,
    private val sessionService: SessionService,
    private val loggedInUserDao: LoggedInUserDao
) : LoginRepository {
    override suspend fun emailLogin(email: String, password: String) {
        try {
            val result = apiLogin.emailLogin(email = email, password = password)
            loggedInUserDao.insert(
                result.profile.toLoggedInUserEntity()
            )
            sessionService.saveToken(result.token)
        } catch (e: HttpException) {
            if (e.code() == 500) {
                throw Exception(e.handle())
            } else {
                throw Exception("알 수 없는 응답이 발생했습니다.(${e.code()})")
            }
        } catch (e: ConnectException) {
            throw Exception("네트워크를 확인해 주세요")
        } catch (e: UnknownHostException) {
            throw Exception("서버 접속 경로에 문제가 발생했습니다.")
        } catch (e: Exception) {
            throw Exception("알 수 없는 오류가 발생했습니다.")
        }
    }

    override suspend fun logout() {
        loggedInUserDao.clear()
        sessionService.removeToken()
    }

    override suspend fun sessionCheck(): Boolean {
        sessionService.getToken()?.let {
            return apiLogin.sessionCheck(it)
        }
        return false;
    }

    override val isLogin: Flow<Boolean> get() = loggedInUserDao.getLoggedInUser().map { it != null }
    override fun getUserName(): Flow<String> {
        return loggedInUserDao.getUserName()
    }

    override suspend fun checkEmail(email: String, password: String): String {
        try {
            return apiLogin.checkEmail(email, password)
        } catch (e: HttpException) {
            throw Exception(e.handle())
        }
    }

    override suspend fun confirmCode(
        token: String,
        confirmCode: String,
        name: String,
        email: String,
        password: String
    ): Boolean {
        try {
            return apiLogin.confirmCode(token, confirmCode, name, email, password);
        } catch (e: HttpException) {
            throw Exception(e.handle())
        }
    }

}

fun RemoteUser.toLoggedInUserEntity(): LoggedInUserEntity {
    return LoggedInUserEntity(
        userId = this.userId,
        userName = this.userName,
        email = this.email,
        loginPlatform = this.loginPlatform,
        createDate = this.createDate,
        profilePicUrl = profilePicUrl
    )
}