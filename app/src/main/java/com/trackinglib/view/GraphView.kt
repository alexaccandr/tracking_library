package com.trackinglib.view

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.SingleStateStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import com.trackinglibrary.model.Track

@StateStrategyType(value = SingleStateStrategy::class)
interface GraphView : MvpView {

    fun onTrackLoaded(tracks: Array<Track>)
}