package com.trackinglib.view

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.support.v4.content.FileProvider
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.arellomobile.mvp.MvpAppCompatFragment
import com.arellomobile.mvp.presenter.InjectPresenter
import com.jakewharton.rxbinding2.widget.RxSeekBar
import com.trackinglib.R
import com.trackinglib.presenter.StartTrackerPresenter
import com.trackinglib.untils.ContextUtils
import com.trackinglibrary.services.RegisterGeofenceService
import com.trackinglibrary.services.RegisterPathsenseService
import com.trackinglibrary.settings.TrackerSettings
import com.trackinglibrary.utils.LogUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_start_tracker.*
import net.ozaydin.serkan.easy_csv.EasyCsv
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread


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

        fun calcValue20(v: Int): Int {
            val res = (20f - 1f) / 100f
            val progressFloat = v.toFloat()
            val res2 = Math.round(res * progressFloat).toFloat()
            val value = (res2 + 1).toInt()
            return value
        }

        fun updateStillSeekBar(value: Int) {
            val result = (value - 1f) / (20f - 1f) * 100f
            stillSeekBar.progress = result.toInt()
        }

        val settings = TrackerSettings(requireContext())
        updateStillSeekBar(settings.getStill())
        updateStillTitle20(settings.getStill())

        val viewDisposable3 = RxSeekBar.changes(stillSeekBar)
            .debounce(10, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { v ->
                    val value = calcValue20(v)
                    settings.executeTransaction {
                        settings.setStill(value)
                    }
                    updateStillTitle20(value)
                }
                , { err ->
                    err.printStackTrace()
                }
            )
        stillSeekBar.progressDrawable.colorFilter = PorterDuffColorFilter(Color.BLUE, PorterDuff.Mode.MULTIPLY)
        stillSeekBar.thumb.setColorFilter(Color.BLUE, PorterDuff.Mode.SRC_IN)
        disposables.add(viewDisposable3)

        fun calcValue150(v: Int): Int {
            val res = (150f - 30f) / 100f
            val progressFloat = v.toFloat()
            val res2 = Math.round(res * progressFloat).toFloat()
            val value = (res2 + 30).toInt()
            return value
        }

        fun updateRadiusSeekBar(value: Int) {
            val result = (value - 30f) / (150f - 30f) * 100f
            seekBar2.progress = result.toInt()
        }

        updateRadiusSeekBar(settings.getGeofenceRadius())
//        val result = calcValue150(settings.getGeofenceRadius())
        updateRadiusTitle150(settings.getGeofenceRadius())

        val viewDisposable2 = RxSeekBar.changes(seekBar2)
            .debounce(10, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { v ->
                    val value = calcValue150(v)
                    settings.executeTransaction {
                        settings.setGeofenceRadius(value)
                    }
                    updateRadiusTitle150(value)
                }
                , { err ->
                    err.printStackTrace()
                }
            )
        seekBar2.progressDrawable.colorFilter = PorterDuffColorFilter(Color.BLUE, PorterDuff.Mode.MULTIPLY)
        seekBar2.thumb.setColorFilter(Color.BLUE, PorterDuff.Mode.SRC_IN)
        disposables.add(viewDisposable2)

        stillButton.setOnClickListener {
            settings.executeTransaction {
                settings.setStillRegistered(!settings.isStillRegistered())
            }
        }

        registerGeofence.setOnClickListener {
            val act = activity
            if (act != null) {
                registerGeofence.isEnabled = false
                if (TextUtils.isEmpty(settings.getGeofenceStr())) {
                    registerGeofence.text = "please wait..."
                    com.trackinglibrary.utils.ContextUtils.startService(act, RegisterGeofenceService::class.java)
                } else {
                    settings.executeTransaction {
                        settings.setGeofenceStr("")
                        com.trackinglibrary.utils.ContextUtils.stopService(act, RegisterGeofenceService::class.java)
                    }
                }
            }
        }

        registerPathsenseGeofence.setOnClickListener {
            val act = activity
            if (act != null) {
                registerPathsenseGeofence.isEnabled = false
                if (TextUtils.isEmpty(settings.getGeofencePathsenseStr())) {
                    registerPathsenseGeofence.text = "please wait..."
                    com.trackinglibrary.utils.ContextUtils.startService(act, RegisterPathsenseService::class.java)
                } else {
                    settings.executeTransaction {
                        settings.setGeofencePathsenseStr("")
                        com.trackinglibrary.utils.ContextUtils.stopService(act, RegisterPathsenseService::class.java)
                    }
                }
            }
        }

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

        saveLogsToCsv.setOnClickListener {
            saveLogsToFile()
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

    private fun updateStillTitle20(value: Int) {
        stillTextView.text = "Still: ${value}"
    }

    private fun updateRadiusTitle150(value: Int) {
        geofenceRadiosTextView.text = "Geofence радиус в метрах: ${value}"
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

    override fun updateGeofence(value: String) {
        registerGeofence.text = if (TextUtils.isEmpty(value)) "Register geofence" else "Remove geofence"
        registerGeofence.isEnabled = true
    }

    override fun updateGeofencePathsense(value: String) {
        registerPathsenseGeofence.text =
            if (TextUtils.isEmpty(value)) "Register Pathsense geofence" else "Remove Pathsense geofence"
        registerPathsenseGeofence.isEnabled = true
    }

    override fun updateStillRegistered(value: Boolean) {
        stillButton.text = if (value) "Disable still" else "Enable still"
    }

    override fun onDestroyView() {
        disposables.dispose()
        super.onDestroyView()
    }

    private fun saveLogsToFile() {
        saveLogsToCsv.isEnabled = false
        thread(start = true) {
            try {
//                LogUtils.deleteDirectory(LogUtils.getExtCacheDir())

                fun readFileAsLinesUsingUseLines(file: File): List<String> = file.useLines { it.toList() }

                val easyCsv = EasyCsv(activity)
                val txtLogFile = File(LogUtils.getExtCacheDir(), LogUtils.autoAccidentFileName)
                LogUtils.convertToCsvDetectorLogs(easyCsv, readFileAsLinesUsingUseLines(txtLogFile))

                val intentShareFile = Intent(Intent.ACTION_SEND)
                val fileWithinMyDir = File(LogUtils.getExtCacheDir(), "auto_accident_detect.csv")

                if (fileWithinMyDir.exists()) {
                    intentShareFile.type = "application/csv"

                    val uri = FileProvider.getUriForFile(
                        context!!,
                        context!!.applicationContext.packageName + ".provider",
                        fileWithinMyDir
                    );
                    intentShareFile.putExtra(Intent.EXTRA_STREAM, uri)

                    intentShareFile.putExtra(
                        Intent.EXTRA_SUBJECT,
                        "Sharing File..."
                    )
                    intentShareFile.putExtra(Intent.EXTRA_TEXT, "Sharing File...")

                    startActivity(Intent.createChooser(intentShareFile, "Share File"))
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                Toast.makeText(requireActivity(), "Save logs error, ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                requireActivity().runOnUiThread {
                    saveLogsToCsv.isEnabled = true
                }
            }
        }
    }
}