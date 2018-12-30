package com.trackinglib.untils

import android.location.Address

object GeocoderUtils {

    fun getAddressLine(address: Address): String {
        val sb = StringBuilder("")
        for (i in 0..address.maxAddressLineIndex) {
            if (i > 0) {
                sb.append(',')
            }
            val line = address.getAddressLine(i)
            if (line == null) {
                sb.append("null")
            } else {
                sb.append('\"')
                sb.append(line)
                sb.append('\"')
            }
        }
        return sb.toString()
    }
}