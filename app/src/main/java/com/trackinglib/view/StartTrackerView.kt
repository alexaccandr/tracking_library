package com.trackinglib.view

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType


interface StartTrackerView : MvpView {
    @StateStrategyType(value = AddToEndSingleStrategy::class, tag = "freq")
    fun updateFrequencyTitle(freq: Long)

    @StateStrategyType(value = SkipStrategy::class, tag = "seek")
    fun updateSeekBar(value: Int)

    @StateStrategyType(value = AddToEndSingleStrategy::class, tag = "trackerStatus")
    fun updateTrackerStatus(started: Boolean)

    @StateStrategyType(value = AddToEndSingleStrategy::class, tag = "recognitionStatus")
    fun updateRecognitionStatus(started: Boolean)
}