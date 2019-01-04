package com.trackinglib.view

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import com.trackinglibrary.model.Track

interface MapView : MvpView {

    @StateStrategyType(value = SkipStrategy::class)
    fun onTrackLoaded(track: Track)

    fun updateStartDate(date: String)
    fun updateStartLocation(address: String)
    fun updateFinishLocation(address: String)
}