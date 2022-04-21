package com.example.pedometer

import android.app.ProgressDialog
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
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.pedometer.LocationService.LocalBinder


class MainActivity : AppCompatActivity() {
    var myService: LocationService? = null
    var locationManager: LocationManager? = null
    var start: Button? = null
    var pause: Button? = null
    var stop: Button? = null
    var image: ImageView? = null
    private val sc: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as LocalBinder
            myService = binder.service
            status = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            status = false
        }
    }

    fun bindService() {
        if (status == true) return
        val i = Intent(applicationContext, LocationService::class.java)
        bindService(i, sc, BIND_AUTO_CREATE)
        status = true
        startTime = System.currentTimeMillis()
    }

    fun unbindService() {
        if (status == false) return
        val i = Intent(applicationContext, LocationService::class.java)
        unbindService(sc)
        status = false
    }

    override fun onDestroy() {
        super.onDestroy()
        if (status == true) unbindService()
    }

    override fun onBackPressed() {
        if (status == false) super.onBackPressed() else moveTaskToBack(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        dist = findViewById<View>(R.id.distancetext) as TextView
        time = findViewById<View>(R.id.timetext) as TextView
        speed = findViewById<View>(R.id.speedtext) as TextView
        start = findViewById<View>(R.id.start) as Button
        pause = findViewById<View>(R.id.pause) as Button
        stop = findViewById<View>(R.id.stop) as Button
        image = findViewById<View>(R.id.image) as ImageView
        start!!.setOnClickListener(View.OnClickListener { //The method below checks if Location is enabled on device or not. If not, then an alert dialog box appears with option
            //to enable gps.
            checkGps()
            locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            if (!locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                return@OnClickListener
            }
            if (status == false) //Here, the Location Service gets bound and the GPS Speedometer gets Active.
                bindService()
            locate = ProgressDialog(this@MainActivity)
            locate!!.isIndeterminate = true
            locate!!.setCancelable(false)
            locate!!.setMessage("Getting Location...")
            locate!!.show()
            start!!.visibility = View.GONE
            pause!!.visibility = View.VISIBLE
            pause!!.text = "Pause"
            stop!!.visibility = View.VISIBLE
        })
        pause!!.setOnClickListener(View.OnClickListener {
            if (pause!!.text.toString().equals("pause", ignoreCase = true)) {
                pause!!.text = "Resume"
                p = 1
            } else if (pause!!.text.toString().equals("Resume", ignoreCase = true)) {
                checkGps()
                locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
                if (!locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    //Toast.makeText(this, "GPS is Enabled in your devide", Toast.LENGTH_SHORT).show();
                    return@OnClickListener
                }
                pause!!.text = "Pause"
                p = 0
            }
        })
        stop!!.setOnClickListener {
            if (status == true) unbindService()
            start!!.visibility = View.VISIBLE
            pause!!.text = "Pause"
            pause!!.visibility = View.GONE
            stop!!.visibility = View.GONE
            p = 0
        }
    }

    //This method leads you to the alert dialog box.
    fun checkGps() {
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (!locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showGPSDisabledAlertToUser()
        }
    }

    //This method configures the Alert Dialog box.
    private fun showGPSDisabledAlertToUser() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setMessage("Enable GPS to use application")
            .setCancelable(false)
            .setPositiveButton(
                "Enable GPS"
            ) { dialog, id ->
                val callGPSSettingIntent = Intent(
                    Settings.ACTION_LOCATION_SOURCE_SETTINGS
                )
                startActivity(callGPSSettingIntent)
            }
        alertDialogBuilder.setNegativeButton(
            "Cancel"
        ) { dialog, id -> dialog.cancel() }
        val alert = alertDialogBuilder.create()
        alert.show()
    }

    companion object {
        var status = false
        var dist: TextView? = null
        var time: TextView? = null
        var speed: TextView? = null
        var startTime: Long = 0
        var endTime: Long = 0
        var locate: ProgressDialog? = null
        var p = 0
    }
}