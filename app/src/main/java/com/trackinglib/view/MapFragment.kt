package com.trackinglib.view

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.trackinglib.R
import com.trackinglibrary.model.GeofenceItem
import com.trackinglibrary.prefs.SettingsController
import com.trackinglibrary.prefs.SettingsControllerListener
import com.trackinglibrary.settings.TrackerSettings


class MapFragment : Fragment(), OnMapReadyCallback {

    lateinit var mMap: GoogleMap

    lateinit var controller: SettingsController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_map, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val settings = TrackerSettings(requireContext())
        controller = SettingsController(requireContext(), object : SettingsControllerListener {
            override fun onChange(context: Context, sharedPreferences: SharedPreferences, key: String) {
                updateGeofenceLocation()
            }
        }, settings.preferences, TrackerSettings.KEY_GEOFENCE_STR, TrackerSettings.KEY_GEOFENCE_PATHSENSE_STR)
    }

    private fun updateGeofenceLocation() {
        val settings = TrackerSettings(requireContext())
        var geofenceStr = settings.getGeofenceStr()
        if (TextUtils.isEmpty(geofenceStr)) {
            geofenceStr = settings.getGeofencePathsenseStr()
        }
        mMap.clear()
        if (geofenceStr.isEmpty()) {
            // ignored
        } else {
            val geofenceObj = Gson().fromJson<GeofenceItem>(geofenceStr, GeofenceItem::class.java)
            if (mMap != null) {
                val latLon = LatLng(geofenceObj.lat, geofenceObj.lon)
                mMap.addCircle(
                    CircleOptions()
                        .center(latLon)
                        .radius(settings.getGeofenceRadius().toDouble())
                        .strokeWidth(3f)
                        .fillColor(resources.getColor(com.trackinglib.R.color.colorRedArgb))
                )

                val cameraPosition = CameraPosition.Builder()
                    .target(latLon)     // Sets the center of the map to location user
                    .zoom(18f)                   // Sets the zoom
                    .build()
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        controller.unregisterListeners()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        updateGeofenceLocation()
    }
}
