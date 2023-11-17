package com.sryang.torang_repository.di.repository.repository.impl

import com.sryang.torang_repository.preference.TorangPreference
import com.sryang.torang_repository.repository.MapRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MapRepositoryImpl @Inject constructor(
    private val torangPreference: TorangPreference
) : MapRepository {
    private var NElat: Double = 0.0
    private var NElon: Double = 0.0
    private var SWlat: Double = 0.0
    private var SWlon: Double = 0.0
    override fun saveLat(lat: Double) {
        torangPreference.saveLat(lat)
    }

    override fun savelon(lon: Double) {
        torangPreference.saveLon(lon)
    }

    override fun saveZoom(level: Float) {
        torangPreference.saveZoom(level)
    }

    override fun loadLat(): Double {
        return torangPreference.loadLat()
    }

    override fun loadLon(): Double {
        return torangPreference.loadLon()
    }

    override fun loadZoom(): Float {
        return torangPreference.loadZoom()
    }

    override fun setNElat(latitude: Double) {
        NElat = latitude
    }

    override fun setNElon(longitude: Double) {
        NElon = longitude
    }

    override fun setSWlat(latitude: Double) {
        SWlat = latitude
    }

    override fun setSWlon(longitude: Double) {
        SWlon = longitude
    }

    override fun getNElat(): Double {
        return NElat
    }

    override fun getNElon(): Double {
        return NElon
    }

    override fun getSWlat(): Double {
        return SWlat
    }

    override fun getSWlon(): Double {
        return SWlon
    }
}