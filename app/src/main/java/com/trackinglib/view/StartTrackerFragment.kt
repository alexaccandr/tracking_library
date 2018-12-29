package com.trackinglib.view

import android.os.Bundle
import android.os.Handler
import android.os.Looper.getMainLooper
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.trackinglib.R
import com.trackinglib.untils.ContextUtils
import com.trackinglibrary.TrackRecorder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_start_tracker.*

class StartTrackerFragment : Fragment() {

    var disposable: Disposable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_start_tracker, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        disposable = TrackRecorder.registerTrackStatusChangeListener(AndroidSchedulers.mainThread()) {
            Handler(getMainLooper()).post {
                updateStatus(it.started)
            }
        }

        updateStatus(TrackRecorder.hasStarted())

        startButton.setOnClickListener {

            val a = activity
            if (a != null) {
                if (ContextUtils.hasLocationPermission(a)) {
                    startButton.isEnabled = false
                    if (TrackRecorder.hasStarted()) {
                        TrackRecorder.stop()
                    } else {
                        TrackRecorder.start()
                    }
                } else {
                    ContextUtils.askForLocationPermission(a)
                }
            }
        }
    }

    private fun updateStatus(started: Boolean) {
        startButton.text = if (started) "Stop tracker" else "Start tracker"
        startButton.isEnabled = true
    }

    override fun onDestroyView() {
        if (disposable != null) {
            disposable!!.dispose()
        }
        super.onDestroyView()
    }
}