package com.example.pedometer

interface LocationServiceCallback {
    fun updateDistance(location: String)
    fun updateSpeed(speed: String)
    fun hideProgress()
}