package com.trackinglib

import android.app.Application
import com.kite.di.AppComponent
import com.kite.di.AppModule
import com.kite.di.DaggerAppComponent
import com.trackinglibrary.TrackRecorder

class App : Application() {

    companion object {
        @JvmStatic
        lateinit var appComponent: AppComponent
    }

    override fun onCreate() {
        super.onCreate()
        createApplicationComponent()
        TrackRecorder.initialize(this)
    }

    private fun createApplicationComponent() {
        appComponent = DaggerAppComponent.builder()
            .appModule(AppModule(this))
            .build()
    }
}
