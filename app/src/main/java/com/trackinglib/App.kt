package com.trackinglib

import android.app.Application
import com.trackinglib.di.AppComponent
import com.trackinglib.di.AppModule
import com.trackinglib.di.DaggerAppComponent
import com.trackinglibrary.TrackRecorder

class App : Application() {

    companion object {
        @JvmStatic
        lateinit var appComponent: AppComponent
    }

    override fun onCreate() {
        super.onCreate()
        createApplicationComponent()
        TrackRecorder.initialize2(this)
    }

    private fun createApplicationComponent() {
        appComponent = DaggerAppComponent.builder()
            .appModule(AppModule(this))
            .build()
    }
}
