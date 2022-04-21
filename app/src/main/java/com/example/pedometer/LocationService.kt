package com.example.pedometer

import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import java.text.DecimalFormat

class LocationService : Service(), LocationListener,
    ConnectionCallbacks, OnConnectionFailedListener {
    private var mLocationRequest: LocationRequest? = null
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mCurrentLocation: Location? = null
    private var lStart: Location? = null
    private var lEnd: Location? = null
    private var speed = 0.0
    private val mBinder: IBinder = LocalBinder()
    private var locationServiceCallback: LocationServiceCallback? = null

    fun setCallback(callback: LocationServiceCallback) {
        locationServiceCallback = callback
    }

    override fun onBind(intent: Intent): IBinder {
        createLocationRequest()
        mGoogleApiClient = GoogleApiClient.Builder(this)
            .addApi(LocationServices.API)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .build()
        mGoogleApiClient?.connect()
        return mBinder
    }

    private fun createLocationRequest() {
        mLocationRequest = LocationRequest.create()
        mLocationRequest!!.interval = INTERVAL
        mLocationRequest!!.fastestInterval = FASTEST_INTERVAL
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onConnected(bundle: Bundle?) {
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this
            )
        } catch (e: SecurityException) {
        }
    }

    private fun stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
            mGoogleApiClient, this
        )
        distance = 0.0
    }

    override fun onConnectionSuspended(i: Int) {}
    override fun onLocationChanged(location: Location) {
        locationServiceCallback?.hideProgress()
        mCurrentLocation = location
        if (lStart == null) {
            lStart = mCurrentLocation
            lEnd = mCurrentLocation
        } else lEnd = mCurrentLocation

        updateUI()
        speed = getSpeedInKmPerHr(location)
    }

    //Calculating the speed with speed property it returns speed in m/s so we are converting it into km/h
    private fun getSpeedInKmPerHr(location: Location) =
        (location.speed * 18 / 5).toDouble()

    override fun onConnectionFailed(connectionResult: ConnectionResult) {}
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
        if (mGoogleApiClient!!.isConnected) mGoogleApiClient!!.disconnect()
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