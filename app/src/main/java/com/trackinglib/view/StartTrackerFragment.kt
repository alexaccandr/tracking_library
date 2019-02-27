package com.trackinglib.view

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arellomobile.mvp.MvpAppCompatFragment
import com.arellomobile.mvp.presenter.InjectPresenter
import com.jakewharton.rxbinding2.widget.RxSeekBar
import com.trackinglib.R
import com.trackinglib.presenter.StartTrackerPresenter
import com.trackinglib.untils.ContextUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_start_tracker.*
import java.util.concurrent.TimeUnit


class StartTrackerFragment : MvpAppCompatFragment(), StartTrackerView {

    private val tagName = StartTrackerFragment::class.java.simpleName
    private val disposables: CompositeDisposable = CompositeDisposable()

    @InjectPresenter
    lateinit var presenter: StartTrackerPresenter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_start_tracker, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startButton.setOnClickListener {

            val a = activity
            if (a != null) {
                if (ContextUtils.hasLocationPermission(a)) {
                    startButton.isEnabled = false
                    presenter.switchTracker()
                } else {
                    ContextUtils.askForLocationPermission(a)
                }
            }
        }

        startRecognitionButton.setOnClickListener {

            val a = activity
            if (a != null) {
                startRecognitionButton.isEnabled = false
                presenter.switchRecognition()
            }
        }

        val viewDisposable = RxSeekBar.changes(seekBar)
            .debounce(100, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { str ->
                    val freq: Long = (str * 0.59).toLong() + 1
                    presenter.updateFrequency(freq)
                }
                , { err ->
                    err.printStackTrace()
                }
            )
        seekBar.progressDrawable.colorFilter = PorterDuffColorFilter(Color.BLUE, PorterDuff.Mode.MULTIPLY)
        seekBar.thumb.setColorFilter(Color.BLUE, PorterDuff.Mode.SRC_IN)
        disposables.add(viewDisposable)
    }

    override fun updateFrequencyTitle(freq: Long) {
        Log.d(tagName, "updateFrequencyTitle($freq)")
        freqTextView.text = "$freq мин."
    }

    override fun updateSeekBar(value: Int) {
        Log.d(tagName, "updateSeekBar($value)")
        seekBar.progress = value
    }

    override fun updateTrackerStatus(started: Boolean) {
        Log.d(tagName, "updateStatus($started)")
        startButton.text = if (started) "Stop tracker" else "Start tracker"
        startButton.isEnabled = true
    }

    override fun updateRecognitionStatus(started: Boolean) {
        Log.d(tagName, "updateStatus($started)")
        startRecognitionButton.text = if (started) "Stop recognition" else "Start recognition"
        startRecognitionButton.isEnabled = true
    }

    override fun onDestroyView() {
        disposables.dispose()
        super.onDestroyView()
    }
}