package com.trackinglib.view

import com.arellomobile.mvp.MvpView
import com.trackinglib.viewmodel.TrackViewModel

interface TracksListView : MvpView {
    fun updateTracksList(tracks: Array<TrackViewModel>)
    fun appendTrack(track: TrackViewModel)
    fun updateTrackLocation(id: String, location: String)
    fun openMapActivity(id: String)
}