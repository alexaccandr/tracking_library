package com.trackinglib.view

import com.arellomobile.mvp.MvpView
import com.trackinglibrary.model.Track

interface GraphView : MvpView{

    fun onTrackLoaded(tracks: Array<Track>)
}