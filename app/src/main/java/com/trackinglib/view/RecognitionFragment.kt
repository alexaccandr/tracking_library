package com.trackinglib.view

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Vibrator
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.android.gms.location.DetectedActivity
import com.google.gson.Gson
import com.trackinglib.R
import com.trackinglib.untils.LogUtils
import com.trackinglibrary.database.LogItem
import com.trackinglibrary.prefs.*
import io.realm.Realm
import kotlinx.android.synthetic.main.fragment_recognition.*
import kotlinx.android.synthetic.main.list_item_recognitio.view.*
import net.ozaydin.serkan.easy_csv.EasyCsv
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread


class RecognitionFragment : Fragment(), SettingsControllerListener {

    var settings: RealTimeSettings? = null
    var settingsController: SettingsController? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_recognition, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        saveLogs.setOnClickListener {
            saveLogsToFile()
        }

        clearData.setOnClickListener {
            clearCollectedData()
        }

        settings = RealTimeSettings(requireContext())
        vibration.isChecked = settings!!.getVibration()
        vibration.setOnCheckedChangeListener { buttonView, isChecked ->
            val settings = RealTimeSettings(requireContext())
            settings.executeTransaction {
                settings.setVibration(isChecked)
            }
        }
        settingsController =
            SettingsController(
                requireContext(), this, settings!!.preferences,
                BaseSettings.SETTING_DATA,
                BaseSettings.SETTING_LOCATION_v2,
                BaseSettings.SETTING_LAST_TRANSITION
            )

        updateLastRecognitionActivity()
    }

    private fun clearCollectedData() {
//        val dialog = AlertDialog.Builder(requireContext())
//        dialog.setTitle("Подтверждение")
//        dialog.setMessage("Удалить накопленные данные?")
//        dialog.setPositiveButton("Удалить") { d, _ ->
//            try {
        Realm.getDefaultInstance().use {
            it.executeTransaction {
                it.delete(LogItem::class.java)
            }
        }
//            } catch (e: Throwable) {
//                Toast.makeText(requireActivity(), "Ошибка при удалении логов", Toast.LENGTH_LONG).show()
//            }
//            d.dismiss()
//        }
//        dialog.setNegativeButton("Отмена") { d, _ ->
//            d.dismiss()
//        }
//        dialog.create().show()
    }

    private fun saveLogsToFile() {
        saveLogs.isEnabled = false
        thread(start = true) {
            try {
                Realm.getDefaultInstance().use {
                    LogUtils.deleteDirectory(LogUtils.getExtCacheDir())
                    val result = it.where(LogItem::class.java).sort("date").findAll()

                    val easyCsv = EasyCsv(activity)
                    LogUtils.writeRecognitionLogs(easyCsv, result)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                Toast.makeText(requireActivity(), "Save logs error, ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                requireActivity().runOnUiThread {
                    saveLogs.isEnabled = true
                }
            }
        }
    }

    override fun onChange(context: Context, sharedPreferences: SharedPreferences, key: String) {

        val context = activity

        context?.runOnUiThread {
            try {
                updateWithKey(key)
            } catch (e: Throwable) {
                Toast.makeText(context, "Somethong wrong with: $key", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateWithKey(key: String) {
        when (key) {
            BaseSettings.SETTING_DATA -> {
                try {
                    if (settings!!.getVibration()) {
                        val v = requireActivity().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                        v.vibrate(50)
                    }


                    val data = settings!!.getData()
                    val item = Gson().fromJson<RecognitionObject>(data, RecognitionObject::class.java)


                    updateTime.text = "last recognition update: ${convertToDateString(item.time)}"

                    val list = item.list.sortedBy { it.type }

                    val adapter = StableArrayAdapter(requireContext(), list)

                    listView.adapter = adapter
                    adapter.notifyDataSetChanged()
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }

            BaseSettings.SETTING_LOCATION_v2 -> {
                val location: MyLocaation =
                    Gson().fromJson<MyLocaation>(settings!!.getLocationV2(), MyLocaation::class.java)

                speed.text = "speed: ${location.speed} m/s"
                accuracy.text = "horiz accuracy: ${location.horAccuracy}"
                bearing.text = "bearing: ${location.bearing}"
            }
            BaseSettings.SETTING_LAST_TRANSITION -> {
                updateLastRecognitionActivity()
            }
        }
    }

    fun convertToDateString(value: Long): String {
        val simpleDateFormat = SimpleDateFormat("HH:mm:ss")
        return simpleDateFormat.format(Date(value))
    }

    private fun updateLastRecognitionActivity() {
        recognTransition.text =
            "last activity transition: ${getActivityString(settings!!.getLastTransition())}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }


    private inner class StableArrayAdapter(
        context: Context,
        objects: List<DetectedActivity>
    ) : ArrayAdapter<DetectedActivity>(context, 0, objects) {


        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = LayoutInflater.from(context).inflate(com.trackinglib.R.layout.list_item_recognitio, null)
            view.type.text = "${getActivityString(getItem(position).type)}"
            view.count.text = "${getItem(position).confidence}%"
            return view
        }
    }

    internal fun getActivityString(detectedActivityType: Int): String {
        when (detectedActivityType) {
            DetectedActivity.ON_BICYCLE -> return "ON_BICYCLE"
            DetectedActivity.ON_FOOT -> return "ON_FOOT"
            DetectedActivity.RUNNING -> return "RUNNING"
            DetectedActivity.STILL -> return "STILL"
            DetectedActivity.TILTING -> return "TITLING"
            DetectedActivity.WALKING -> return "WALKING"
            DetectedActivity.IN_VEHICLE -> return "IN_VEHICLE"
            else -> return "SOMETHING_ELSE: $detectedActivityType"
        }
    }
}