package com.trackinglibrary.utils

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.PutObjectRequest
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object S3Publisher {

    fun publish(
        file: File,
        fileFormat: String,
        fileFolder: String,
        deviceName: String,
        appVersion: String
    ) {
        val s3Client = AmazonS3Client(
            BasicAWSCredentials("AKIAJTHGRPT2M5YIMVCQ", "ReBTcgED8SSGzKcrbX43Xp/VDQZc9QQzPybIoFCc"),
            Region.getRegion(Regions.US_EAST_1)
        )

        val dateStr = SimpleDateFormat("yyyy dd mm, hh:mm:ss", Locale.US).format(Date().time)
        val fileName = "$dateStr.$fileFormat $deviceName $appVersion"
        val por = PutObjectRequest("s3.raxeltelematis.logs.bucket/$fileFolder", fileName, file);
        s3Client.putObject(por);
    }
}