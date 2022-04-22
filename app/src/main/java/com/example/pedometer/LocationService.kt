package com.example.pedometer

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Binder
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import java.text.DecimalFormat

class LocationService : Service() {
    private var mLocationRequest: LocationRequest? = null
    private var mCurrentLocation: Location? = null
    private var lStart: Location? = null
    private var lEnd: Location? = null
    private var speed = 0.0
    private val mBinder: IBinder = LocalBinder()
    private var locationServiceCallback: LocationServiceCallback? = null
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null

    fun setCallback(callback: LocationServiceCallback) {
        locationServiceCallback = callback
    }

    private val mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            if (locationResult != null) {
                for (location in locationResult.locations) {
                    locationServiceCallback?.hideProgress()
                    mCurrentLocation = location
                    if (lStart == null) {
                        lStart = mCurrentLocation
                        lEnd = mCurrentLocation
                    } else lEnd = mCurrentLocation

                    updateUI()
                    speed = getSpeedInKmPerHr(location)
                }
            }
        }
    }

    override fun onBind(intent: Intent): IBinder {
        createLocationRequest()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationProviderClient?.requestLocationUpdates(
                mLocationRequest,
                mLocationCallback,
                Looper.myLooper()
            )
        }
        return mBinder
    }

    private fun createLocationRequest() {
        mLocationRequest = LocationRequest.create()
        mLocationRequest!!.interval = INTERVAL
        mLocationRequest!!.fastestInterval = FASTEST_INTERVAL
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private fun stopLocationUpdates() {
        fusedLocationProviderClient?.removeLocationUpdates(mLocationCallback)
        distance = 0.0
    }

    //Calculating the speed with speed property it returns speed in m/s so we are converting it into km/h
    private fun getSpeedInKmPerHr(location: Location) =
        (location.speed * 18 / 5).toDouble()

    inner class LocalBinder : Binder() {
        val service: LocationService
            get() = this@LocationService
    }

    /*
    Calling the method below updates the live values of distance and speed to the TextViews.
    The live feed of Distance and Speed are being set in the method below.
    */
    private fun updateUI() {
        if (!MainActivity.isPaused) {
            distance += lStart!!.distanceTo(lEnd) / 1000.00
            MainActivity.endTime = System.currentTimeMillis()
            /*var diff = MainActivity.endTime - MainActivity.startTime
            diff = TimeUnit.MILLISECONDS.toMinutes(diff)*/
            val currentSpeed: String = if (speed > 0.0)
                "Current speed: " + DecimalFormat("#.##").format(speed) + " km/hr" else "Calculating..."

            locationServiceCallback?.updateSpeed(currentSpeed)
            locationServiceCallback?.updateDistance(DecimalFormat("#.###").format(distance) + " Km's.")
            lStart = lEnd
        }
    }

    override fun onUnbind(intent: Intent): Boolean {
        stopLocationUpdates()
        lStart = null
        lEnd = null
        distance = 0.0
        return super.onUnbind(intent)
    }

    companion object {
        private const val INTERVAL = (1000 * 2).toLong()
        private const val FASTEST_INTERVAL = (1000 * 1).toLong()
        var distance = 0.0
    }
}