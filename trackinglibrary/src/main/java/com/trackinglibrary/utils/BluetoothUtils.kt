package com.trackinglibrary.utils

import android.bluetooth.BluetoothAdapter

object BluetoothUtils {
    fun enableBluetooth(): Boolean {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        return bluetoothAdapter.isEnabled
    }
}