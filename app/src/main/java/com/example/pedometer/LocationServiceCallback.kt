package com.example.pedometer

interface LocationServiceCallback {
    fun updateTime(time: String)
    fun updateLocation(location: String)
    fun updateSpeed(speed: String)
    fun hideProgress()
}