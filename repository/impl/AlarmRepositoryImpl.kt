package com.sryang.torang_repository.di.repository.repository.impl

import com.sryang.torang_repository.api.ApiAlarm
import com.sryang.torang_repository.data.RemoteAlarm
import com.sryang.torang_repository.repository.AlarmRepository
import com.sryang.torang_repository.session.SessionService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmRepositoryImpl @Inject constructor(
    private val apiAlarm: ApiAlarm,
    override val isLogin: Flow<Boolean>,
    private val sessionService: SessionService
) : AlarmRepository {


    override suspend fun loadAlarm(): List<RemoteAlarm> {
        var list = ArrayList<RemoteAlarm>()
        sessionService.getToken()?.let {
            list = apiAlarm.getAlarms(it)
        }
        return list
    }

    override suspend fun deleteAlarm() {

    }
}