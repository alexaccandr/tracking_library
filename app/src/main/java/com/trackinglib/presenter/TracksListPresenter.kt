package com.trackinglib.presenter

import android.location.Geocoder
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.trackinglib.App
import com.trackinglib.model.geocoder.GeoCoderApi
import com.trackinglib.untils.ViewModelAdapter
import com.trackinglib.view.TracksListView
import com.trackinglibrary.TrackRecorder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

@InjectViewState
class TracksListPresenter : MvpPresenter<TracksListView>() {

    private var disposables: CompositeDisposable? = null
    private var lastTrackLocationUpdated = false

    @Inject
    lateinit var geocoder: Geocoder

    init {
        App.appComponent.inject(this)
    }

    fun init() {
        disposables = CompositeDisposable()
        disposables!!.add(TrackRecorder.registerTrackStatusChangeListener(AndroidSchedulers.mainThread()) {
            if (it.started) {
                lastTrackLocationUpdated = false
                viewState.appendTrack(ViewModelAdapter.adaptTrack(it.track))
                if (it.track.locations.isNotEmpty()) {
                    lastTrackLocationUpdated = true
                    updateLocation(it.track.id, it.track.locations[0].lat, it.track.locations[0].lon)
                }
            }
        })
        disposables!!.add(TrackRecorder.registerTrackLocationChangeListener(AndroidSchedulers.mainThread()) {
            if (!lastTrackLocationUpdated) {
                lastTrackLocationUpdated = true
                updateLocation(it.trackId, it.lat, it.lon)
            }
        })

        val tracks = TrackRecorder.getTracks()
        tracks.forEach {
            if (it.locations.isNotEmpty()) {
                updateLocation(it.id, it.locations[0].lat, it.locations[0].lon)
            }
        }
        viewState.updateTracksList(tracks.map {
            ViewModelAdapter.adaptTrack(it)
        }.toTypedArray())
    }

    private fun updateLocation(id: String, lat: Double, lon: Double) {
        val d = GeoCoderApi.execute(geocoder, lat, lon) { address ->
            if (address.locality != null) {
                viewState.updateTrackLocation(id, address.locality)
            }
        }
        disposables!!.add(d)
    }

    override fun onDestroy() {
        super.onDestroy()
        val d = disposables
        if (d != null && !d.isDisposed) {
            d.dispose()
        }
    }
}