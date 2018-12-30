package com.trackinglib.presenter

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.trackinglib.view.GraphView
import com.trackinglibrary.TrackRecorder

@InjectViewState
class GraphPresenter : MvpPresenter<GraphView>() {

    fun init() {
        val tracks = TrackRecorder.getTracks().sortedBy { it.startDate }.toTypedArray()
        viewState.onTrackLoaded(tracks)
    }
}