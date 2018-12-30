package com.trackinglib.presenter

import android.location.Geocoder
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.trackinglib.App
import com.trackinglib.model.geocoder.GeoCoderApi
import com.trackinglib.untils.GeocoderUtils
import com.trackinglib.view.MapView
import com.trackinglibrary.TrackRecorder
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@InjectViewState
class MapPresenter : MvpPresenter<MapView>() {

    @Inject
    lateinit var geocoder: Geocoder

    init {
        App.appComponent.inject(this)
    }

    fun init(trackId: String?) {
        if (trackId != null) {
            val track = TrackRecorder.getTrack(trackId)
            viewState.onTrackLoaded(track)

            // start date
            val format = SimpleDateFormat("d MMMM, yyyy hh:mm:ss", Locale.US)
            viewState.updateStartDate(format.format(track.startDate))

            if (track.locations.isNotEmpty()) {
                GeoCoderApi.execute(geocoder, track.locations.first().lat, track.locations.first().lon) {
                    viewState.updateStartLocation(GeocoderUtils.getAddressLine(it))
                }

                if (track.finishDate == null) {
                    viewState.updateFinishLocation("Трек не завершен, идет запись...")
                } else {
                    GeoCoderApi.execute(geocoder, track.locations.last().lat, track.locations.last().lon) {
                        viewState.updateFinishLocation(GeocoderUtils.getAddressLine(it))
                    }
                }
            }
        }
    }
}