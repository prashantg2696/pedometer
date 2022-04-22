package com.example.pedometer.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.pedometer.R
import com.google.android.material.snackbar.Snackbar

object PermissionUtil {

    fun checkPermissionWithExplanation(
        activity: Activity,
        permission: String,
        activityResultLauncher: ActivityResultLauncher<String>,
    ): Boolean {
        return when {
            hasPermissionGranted(activity, permission) -> true
            showShowRationaleDialog(activity, permission) -> {
                val explanation = getExplanationForPermission(activity, permission)
                val viewGroup = (activity
                    .findViewById<View>(android.R.id.content) as ViewGroup).getChildAt(0) as ViewGroup

                Snackbar.make(viewGroup, explanation, Snackbar.LENGTH_INDEFINITE)
                    .setAction("Ok") {
                        activityResultLauncher.launch(permission)
                    }
                    .setActionTextColor(
                        ContextCompat.getColor(
                            activity.applicationContext,
                            R.color.white
                        )
                    )
                    .show()
                false
            }
            else -> {
                activityResultLauncher.launch(permission)
                false
            }
        }
    }

    private fun hasPermissionGranted(activity: Activity, permission: String) =
        ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED

    private fun showShowRationaleDialog(activity: Activity, permission: String) =
        ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)

    private fun getExplanationForPermission(context: Context, permission: String): String {
        return when (permission) {
            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION ->
                context.getString(R.string.location_permission_explanation)
            else -> ""
        }
    }
}
