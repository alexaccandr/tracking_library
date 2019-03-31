package com.trackinglib.presenter

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.trackinglib.App
import com.trackinglib.view.StartTrackerView
import com.trackinglibrary.TrackRecorder
import com.trackinglibrary.prefs.SettingsController
import com.trackinglibrary.prefs.SettingsControllerListener
import com.trackinglibrary.settings.TrackerSettings
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@InjectViewState
class StartTrackerPresenter : MvpPresenter<StartTrackerView>() {

    var disposables: CompositeDisposable = CompositeDisposable()
    @Inject
    lateinit var context: Context
    @Inject
    lateinit var settings: TrackerSettings
    private var listener: SettingsControllerListener? = null
    private var settingsController: SettingsController? = null

    init {
        App.appComponent.inject(this)
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        viewState.updateTrackerStatus(TrackRecorder.hasStarted())
        viewState.updateRecognitionStatus(TrackRecorder.hasStartedRecognition())
        viewState.updateGeofence(TrackerSettings(context).getGeofenceStr())
        viewState.updateGeofencePathsense(TrackerSettings(context).getGeofencePathsenseStr())
        viewState.updateStillRegistered(TrackerSettings(context).isStillRegistered())

        val tracksDisposable = TrackRecorder.registerTrackStatusChangeListener(AndroidSchedulers.mainThread()) {
            Handler(Looper.getMainLooper()).post {
                viewState.updateTrackerStatus(it.started)
            }
        }
        disposables.add(tracksDisposable)


        listener = object : SettingsControllerListener {
            override fun onChange(context: Context, sharedPreferences: SharedPreferences, key: String) {
                when (key) {
                    TrackerSettings.KEY_RECOGNITION_STARTED -> viewState.updateRecognitionStatus(TrackRecorder.hasStartedRecognition())
                    TrackerSettings.KEY_GEOFENCE_STR -> viewState.updateGeofence(TrackerSettings(context).getGeofenceStr())
                    TrackerSettings.KEY_GEOFENCE_PATHSENSE_STR -> viewState.updateGeofencePathsense(
                        TrackerSettings(
                            context
                        ).getGeofencePathsenseStr()
                    )
                    TrackerSettings.KEY_STILL_REGISTERED -> viewState.updateStillRegistered(TrackerSettings(context).isStillRegistered())
                }
            }
        }
        settingsController =
            SettingsController(
                context, listener!!, settings.preferences,
                TrackerSettings.KEY_RECOGNITION_STARTED,
                TrackerSettings.KEY_GEOFENCE_STR,
                TrackerSettings.KEY_GEOFENCE_PATHSENSE_STR,
                TrackerSettings.KEY_STILL_REGISTERED
            )
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
        if (settingsController != null) {
            settingsController!!.unregisterListeners()
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

    fun switchRecognition() {
        if (TrackRecorder.hasStartedRecognition()) {
            TrackRecorder.stopRecognition()
        } else {
            TrackRecorder.startRecognition()
        }
    }
}