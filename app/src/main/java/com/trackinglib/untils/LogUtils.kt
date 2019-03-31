package com.trackinglib.untils

import android.os.Environment
import android.text.TextUtils
import android.util.Log
import com.trackinglibrary.database.LogItem
import net.ozaydin.serkan.easy_csv.EasyCsv
import net.ozaydin.serkan.easy_csv.FileCallback
import java.io.*
import java.nio.channels.FileChannel
import java.text.SimpleDateFormat
import java.util.*


object LogUtils {

    private val format = SimpleDateFormat("d HH:mm:ss", Locale.US)

    fun writeRecognitionLogs(easyCsv: EasyCsv, recognitionItems: List<LogItem>) {

        easyCsv.setSeparatorColumn("*");
        easyCsv.setSeperatorLine("?");
        val headerList = ArrayList<String>()

        val dataList = ArrayList<String>()
//        dataList.add("Serkan.Ozaydin.23-")

        dataList.apply {
            add("Time*STILL*IN VEHICLE*BICYCLE*FOOT*RUNNING*TITLING*WALKING*SPEED km/h?")
        }
        val file = createFileWithName("accelerometer_still_vehicle.csv")
//        BufferedWriter(FileWriter(file)).use {

        fun write(formatted: String) {
//                it.write(formatted)
//                it.newLine()
        }

        write("S - still,\nV - IN_VEHICLE,\nSP - SPEED in km/h")
        write(String.format("%-15s%-7s%-7s%s", "", "S", "V", "SP"))
        recognitionItems.forEachIndexed { it1, it2 ->
            //                if (it1 % 30 == 0) {
//                    write(String.format("%-15s%-7s%s", "", "S", "V"))
//                }
//            "${findConfidence(DetectedActivity.ON_BICYCLE)}," +
//                    "${findConfidence(DetectedActivity.ON_FOOT)}," +
//                    "${findConfidence(DetectedActivity.RUNNING)}," +
//                    "${findConfidence(DetectedActivity.STILL)}," +
//                    "${findConfidence(DetectedActivity.TILTING)}," +
//                    "${findConfidence(DetectedActivity.WALKING)}," +
//                    findConfidence(DetectedActivity.IN_VEHICLE)
            when (it2.type) {
                LogItem.Companion.Type.TYPE_ACCELEROMETER.typeId -> {
                    val parsedData = parseActivityRecognitionData(it2)
                    val stillStr = if (TextUtils.isEmpty(parsedData.second[3])) "-" else parsedData.second[3]
                    val inVehicleStr = if (TextUtils.isEmpty(parsedData.second[6])) "-" else parsedData.second[6]
                    val onBicycleStr = if (TextUtils.isEmpty(parsedData.second[0])) "-" else parsedData.second[0]
                    val onFootStr = if (TextUtils.isEmpty(parsedData.second[1])) "-" else parsedData.second[1]
                    val onRunningStr = if (TextUtils.isEmpty(parsedData.second[2])) "-" else parsedData.second[2]
                    val titlingStr = if (TextUtils.isEmpty(parsedData.second[4])) "-" else parsedData.second[4]
                    val walkingStr = if (TextUtils.isEmpty(parsedData.second[5])) "-" else parsedData.second[5]

                    write(
                        String.format(
                            "%-15s%-7s%-7s",
                            format.format(parsedData.first),
                            stillStr,
                            inVehicleStr
                        )
                    )
                    dataList.add("${format.format(parsedData.first)}*${stillStr}*${inVehicleStr}*${onBicycleStr}*${onFootStr}*${onRunningStr}*${titlingStr}*${walkingStr}?")
                }
                LogItem.Companion.Type.TYPE_SPEED.typeId -> {
                    val parsedData = parseActivityRecognitionData(it2)
                    val speedStr = if (TextUtils.isEmpty(parsedData.second[0])) "0.0" else parsedData.second[0]
                    write(
                        String.format(
                            "%-29s%.1f",
                            format.format(parsedData.first),
                            java.lang.Float.parseFloat(speedStr)
                        )
                    )
                    dataList.add(
                        "${format.format(parsedData.first)}* * * * * * * *${String.format(
                            Locale.US,
                            "%.1f",
                            java.lang.Float.parseFloat(speedStr)
                        )}?"
                    )
                }
            }
        }

        easyCsv.createCsvFile("logs", headerList, dataList, 123, object : FileCallback {
            override fun onSuccess(p0: File?) {
                Log.e("tag", "tag")
                val from = p0
                moveFile(from!!, getExtCacheDir())
            }

            override fun onFail(p0: String?) {
                Log.e("tag", "tag")
            }
        })
//        }
    }

    @Throws(IOException::class)
    private fun moveFile(file: File, dir: File) {
        val newFile = File(dir, file.name)
        var outputChannel: FileChannel? = null
        var inputChannel: FileChannel? = null
        try {
            outputChannel = FileOutputStream(newFile).getChannel()
            inputChannel = FileInputStream(file).getChannel()
            inputChannel!!.transferTo(0, inputChannel!!.size(), outputChannel)
            inputChannel!!.close()
            file.delete()
        } finally {
            if (inputChannel != null) inputChannel!!.close()
            if (outputChannel != null) outputChannel!!.close()
        }

    }

    private fun parseActivityRecognitionData(logItem: LogItem): Pair<Long, Array<String>> {
        val recognitionList: Array<String> = when (logItem.type) {
            LogItem.Companion.Type.TYPE_ACCELEROMETER.typeId -> {
                val items = logItem.data.split(",")
                arrayOf(items[1], items[2], items[3], items[4], items[5], items[6], items[7])
            }
            LogItem.Companion.Type.TYPE_SPEED.typeId -> {
                arrayOf(logItem.data)
            }
            else -> arrayOf()
        }
        return Pair(logItem.date, recognitionList)
    }

    @Throws(IOException::class)
    private fun createFileWithName(fileName: String): File {
        val dir = getExtCacheDir()
        val file = File(dir, "$fileName.txt")
        if (file.exists()) {
            val result = file.delete()
            if (!result) {
                throw IOException("delete file problem")
            }
        }
        val createResult = file.createNewFile()
        if (!createResult) {
            throw IOException("create file problem")
        }

        return file
    }

    @Throws(IOException::class)
    fun getExtCacheDir(): File {

        @Throws(IOException::class)
        fun checkAndCreateDir(dir: String): File {
            val file = File(dir)
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    throw IOException("Can't create dir, $dir")
                }
            }
            return file
        }

        val cacheDir = String.format("%s/%s", Environment.getExternalStorageDirectory().absolutePath, "test_logs")
        return checkAndCreateDir(cacheDir)
    }

    fun deleteDirectory(dir: File) {
        delete(dir)
    }

    @Throws(IOException::class)
    private fun delete(f: File) {
        if (f.isDirectory) {
            for (c in f.listFiles()!!)
                delete(c)
        }
        if (!f.delete())
            throw FileNotFoundException("Failed to delete file: $f")
    }
}