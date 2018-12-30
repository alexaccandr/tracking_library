package com.trackinglib.view

import com.arellomobile.mvp.MvpView
import com.trackinglibrary.model.Track

interface MapView : MvpView {

    fun onTrackLoaded(track: Track)
    fun updateStartDate(date: String)
    fun updateStartLocation(address: String)
    fun updateFinishLocation(address: String)
}