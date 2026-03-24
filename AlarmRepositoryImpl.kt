package com.sarang.torang.di.repository

import com.sarang.torang.api.ApiAlarm
import com.sarang.torang.data.Alarm
import com.sarang.torang.data.User
import com.sarang.torang.data.remote.response.AlarmApiModel
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


    override suspend fun loadAlarm(): List<Alarm> {
        var list : List<AlarmApiModel> = emptyList()
        sessionService.getToken()?.let {
            apiAlarm.findAll(it).body()?.let {
                list = it
            }
        }
        return list.map { Alarm.fromApiModel(it) }
    }

    override suspend fun deleteAlarm() {

    }
}