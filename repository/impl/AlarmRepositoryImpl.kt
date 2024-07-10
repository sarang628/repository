package com.sarang.torang.di.repository.repository.impl

import com.sarang.torang.api.ApiAlarm
import com.sarang.torang.data.remote.response.AlarmAlarmModel
import com.sarang.torang.repository.AlarmRepository
import com.sarang.torang.session.SessionService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmRepositoryImpl @Inject constructor(
    private val apiAlarm: ApiAlarm,
    val isLogin: Flow<Boolean>,
    private val sessionService: SessionService
) : AlarmRepository {


    override suspend fun loadAlarm(): List<AlarmAlarmModel> {
        var list = ArrayList<AlarmAlarmModel>()
        sessionService.getToken()?.let {
            list = apiAlarm.getAlarms(it)
        }
        return list
    }

    override suspend fun deleteAlarm() {

    }
}