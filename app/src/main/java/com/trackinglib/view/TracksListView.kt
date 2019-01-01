package com.trackinglib.view

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.AddToEndStrategy
import com.arellomobile.mvp.viewstate.strategy.SingleStateStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import com.trackinglib.viewmodel.TrackViewModel

@StateStrategyType(value = AddToEndStrategy::class)
interface TracksListView : MvpView {
    fun updateTracksList(tracks: Array<TrackViewModel>)
    fun appendTrack(track: TrackViewModel)
    fun updateTrackLocation(id: String, location: String)
    fun openMapActivity(id: String)
}