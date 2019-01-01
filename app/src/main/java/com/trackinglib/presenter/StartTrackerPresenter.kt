package com.trackinglib.presenter

import android.os.Handler
import android.os.Looper
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.trackinglib.view.StartTrackerView
import com.trackinglibrary.TrackRecorder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

@InjectViewState
class StartTrackerPresenter : MvpPresenter<StartTrackerView>() {

    var disposables: CompositeDisposable = CompositeDisposable()

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        viewState.updateStatus(TrackRecorder.hasStarted())

        val tracksDisposable = TrackRecorder.registerTrackStatusChangeListener(AndroidSchedulers.mainThread()) {
            Handler(Looper.getMainLooper()).post {
                viewState.updateStatus(it.started)
            }
        }
        disposables.add(tracksDisposable)
    }

    override fun attachView(view: StartTrackerView?) {
        super.attachView(view)
        val freq = TrackRecorder.getFrequency()
        val freqInMinutes = TimeUnit.MILLISECONDS.toMinutes(freq)
        viewState.updateFrequencyTitle(freqInMinutes)
        viewState.updateSeekBar(((freqInMinutes) / 0.59).toInt())
    }

    fun updateFrequency(frequencyInMinutes: Long) {
        TrackRecorder.setFrequency(TimeUnit.MINUTES.toMillis(frequencyInMinutes))
        viewState.updateFrequencyTitle(frequencyInMinutes)
    }

    override fun onDestroy() {
        if (!disposables.isDisposed) {
            disposables.dispose()
        }
        super.onDestroy()
    }

    fun switchTracker() {
        if (TrackRecorder.hasStarted()) {
            TrackRecorder.stop()
        } else {
            TrackRecorder.start()
        }
    }
}