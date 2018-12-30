package com.trackinglib.view

import android.os.Bundle
import com.arellomobile.mvp.MvpAppCompatActivity
import com.arellomobile.mvp.presenter.InjectPresenter
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.trackinglib.R
import com.trackinglib.presenter.MapPresenter
import com.trackinglibrary.model.Track
import kotlinx.android.synthetic.main.activity_maps.*

class MapsActivity : MvpAppCompatActivity(), OnMapReadyCallback, MapView {

    companion object {
        val EXTRA_TRACK_ID = "extra_track_id"
    }

    @InjectPresenter
    lateinit var presenter: MapPresenter
    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val extras = intent.extras
        val trackId = extras.getString(EXTRA_TRACK_ID, null)
        presenter.init(trackId)
    }

    override fun onTrackLoaded(track: Track) {
        if (track.locations.isNotEmpty()) {
            track.locations.forEachIndexed { index, point ->
                val latLonPoint = LatLng(point.lat, point.lon)
                mMap.addMarker(MarkerOptions().position(latLonPoint))
                if (index == track.locations.size - 1) {
//                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLonPoint))
                    mMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            latLonPoint,
                            10.0f
                        )
                    )
                }
            }

            if (track.locations.size > 1) {
                mMap.addPolyline(
                    (PolylineOptions())
                        .clickable(true)
                        .addAll(track.locations
                            .sortedBy { it.date }
                            .map {
                                LatLng(it.lat, it.lon)
                            })
                )
            }
        }
    }

    override fun updateStartDate(date: String) {
        startDateView.text = date
    }

    override fun updateStartLocation(address: String) {
        locationView.text = address
    }

    override fun updateFinishLocation(address: String) {
        locationEndView.text = address
    }
}
