package com.sryang.torang_repository.di.repository.repository.impl

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationPreferencesImpl @Inject constructor(@ApplicationContext val context: Context)
    //: LocationPreferences
{
//    override suspend fun isFirstRequestLocationPermission(): Boolean {
//        val b = getPref().getBoolean("isFirstRequestLocationPermission", false)
//        return b
//    }

//    override fun requestLocationPermission() {
//        getPref().edit()
//            .putBoolean("isFirstRequestLocationPermission", true)
//            .commit()
//    }

    fun getPref(): SharedPreferences {
        return context.getSharedPreferences("torang", Context.MODE_PRIVATE)
    }
}