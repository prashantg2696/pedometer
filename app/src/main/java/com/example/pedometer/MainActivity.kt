package com.example.pedometer

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.pedometer.LocationService.LocalBinder
import com.example.pedometer.utils.PermissionUtil


class MainActivity : AppCompatActivity(), LocationServiceCallback {
    private lateinit var locationService: LocationService
    private lateinit var locationManager: LocationManager
    private lateinit var startButton: Button
    private lateinit var pauseButton: Button
    private lateinit var stopButton: Button
    private lateinit var distanceTextView: TextView
    private lateinit var speedTextView: TextView
    private lateinit var imageView: ImageView
    private lateinit var progressBar: ProgressBar

    private var permissionResultLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (it) {
            startCapturing()
        }
    }

    private var locationResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
            startCapturing()
    }

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as LocalBinder
            locationService = binder.service
            locationService.setCallback(this@MainActivity)
            status = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            status = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewReferences()
        viewClickEvents()
    }

    private fun viewReferences() {
        distanceTextView = findViewById(R.id.distancetext)
        speedTextView = findViewById(R.id.speedtext)
        startButton = findViewById(R.id.start)
        pauseButton = findViewById(R.id.pause)
        stopButton = findViewById(R.id.stop)
        imageView = findViewById(R.id.image)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun viewClickEvents() {
        startButton.setOnClickListener {
            if (PermissionUtil.checkPermissionWithExplanation(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    permissionResultLauncher
                )
            ) {
                startCapturing()
            }
        }
        pauseButton.setOnClickListener {
            pauseCapturing()
        }
        stopButton.setOnClickListener {
            stopCapturing()
        }
    }

    private fun stopCapturing() {
        if (status) unbindService()
        startButton.visibility = View.VISIBLE
        pauseButton.text = getString(R.string.pause)
        pauseButton.visibility = View.GONE
        stopButton.visibility = View.GONE
        isPaused = false
    }

    private fun pauseCapturing() {
        if (pauseButton.text.toString().equals("pause", ignoreCase = true)) {
            pauseButton.text = getString(R.string.resume)
            isPaused = true
        } else if (pauseButton.text.toString().equals("Resume", ignoreCase = true)) {
            checkGps()
            locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                return
            }
            pauseButton.text = getString(R.string.pause)
            isPaused = false
        }
    }

    private fun startCapturing() {
        checkGps()
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return
        }
        if (!status) //Here, the Location Service gets bound and the GPS Speedometer gets Active.
            bindService()
        progressBar.isVisible = true
        startButton.visibility = View.GONE
        pauseButton.visibility = View.VISIBLE
        pauseButton.text = getString(R.string.pause)
        stopButton.visibility = View.VISIBLE
    }


    /*The method below checks if Location is enabled on device or not. If not, then an alert dialog box appears with option
      to enable gps. This method leads you to the alert dialog box.*/
    private fun checkGps() {
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showGPSDisabledDialog()
        }
    }

    //This method configures the Alert Dialog box.
    private fun showGPSDisabledDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setMessage("Please enable GPS")
            .setCancelable(false)
            .setPositiveButton(
                "Enable GPS"
            ) { _, _ ->
                locationResultLauncher.launch(
                    Intent(
                        Settings.ACTION_LOCATION_SOURCE_SETTINGS
                    )
                )
            }
        alertDialogBuilder.setNegativeButton(
            "Cancel"
        ) { dialog, _ -> dialog.cancel() }
        val alert = alertDialogBuilder.create()
        alert.show()
    }

    companion object {
        var status = false
        var startTime: Long = 0
        var endTime: Long = 0
        var isPaused = false
    }

    override fun updateDistance(location: String) {
        distanceTextView.text = location
    }

    override fun updateSpeed(speed: String) {
        speedTextView.text = speed
    }

    override fun hideProgress() {
        progressBar.isVisible = false
    }

    private fun bindService() {
        if (status) return
        val intent = Intent(applicationContext, LocationService::class.java)
        bindService(intent, serviceConnection, BIND_AUTO_CREATE)
        status = true
        startTime = System.currentTimeMillis()
    }

    private fun unbindService() {
        if (!status) return
        Intent(applicationContext, LocationService::class.java)
        unbindService(serviceConnection)
        status = false
    }

    override fun onDestroy() {
        super.onDestroy()
        if (status) unbindService()
    }

    override fun onBackPressed() {
        if (!status) super.onBackPressed() else moveTaskToBack(true)
    }
}