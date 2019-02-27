package com.trackinglib.untils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat

object ContextUtils {

    val locationRequestCode = 0x1111
    val storageRequestCode = 0x1112

    fun checkPermission(context: Activity): Boolean {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_DENIED
        ) {
            return false
        }
        return true
    }

    fun askForLocationPermission(context: Activity) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {

                //This is called if user has denied the permission before
                //In this case I am just asking the permission again
                ActivityCompat.requestPermissions(
                    context, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    locationRequestCode
                )

            } else {
                ActivityCompat.requestPermissions(
                    context, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    locationRequestCode
                )
            }
        } else {
            // ignored
//            Toast.makeText(this, "$permission is already granted.", Toast.LENGTH_SHORT).show()
        }
    }

    fun askForStoragePermission(context: Activity) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            ) {

                //This is called if user has denied the permission before
                //In this case I am just asking the permission again
                ActivityCompat.requestPermissions(
                    context, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    storageRequestCode
                )

            } else {
                ActivityCompat.requestPermissions(
                    context, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    storageRequestCode
                )
            }
        } else {
            // ignored
//            Toast.makeText(this, "$permission is already granted.", Toast.LENGTH_SHORT).show()
        }
    }

    fun hasLocationPermission(activity: Activity): Boolean {
        return checkPermission(activity)
    }
}