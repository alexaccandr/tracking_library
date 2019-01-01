package com.trackinglib.presenter

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.trackinglib.view.GraphView
import com.trackinglibrary.TrackRecorder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

@InjectViewState
class GraphPresenter : MvpPresenter<GraphView>() {

    var disposable: CompositeDisposable = CompositeDisposable()

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        updateGraph()

        disposable.add(TrackRecorder.registerTrackStatusChangeListener(AndroidSchedulers.mainThread()) {
            updateGraph()
        })
        disposable.add(TrackRecorder.registerTrackLocationChangeListener(AndroidSchedulers.mainThread()) {
            updateGraph()
        })
    }

    private fun updateGraph() {
        val tracks = TrackRecorder.getTracks().sortedBy { it.startDate }.toTypedArray()
        viewState.onTrackLoaded(tracks)
    }

    override fun onDestroy() {
        if (!disposable.isDisposed) {
            disposable.dispose()
        }
        super.onDestroy()
    }
}