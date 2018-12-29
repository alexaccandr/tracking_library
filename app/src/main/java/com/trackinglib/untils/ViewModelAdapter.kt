package com.trackinglib.untils

import com.trackinglib.viewmodel.TrackViewModel
import com.trackinglibrary.model.Track

object ViewModelAdapter {

    fun adaptTrack(track: Track): TrackViewModel {
        return TrackViewModel(track.id, track.startDate, null)
    }
}