package com.sarang.torang.di.repository.data

enum class Distances(val value : String) {
    NONE(""),
    _100M("100m"),
    _300M("300m"),
    _500M("500m"),
    _1KM("1km"),
    _3KM("3km");

    companion object{
        fun findByString(value : String) : Distances{
            return Distances.entries.find { it.value == value } ?: return NONE
        }
    }
}
