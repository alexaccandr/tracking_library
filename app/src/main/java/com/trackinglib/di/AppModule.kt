package com.trackinglib.di

import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.location.Geocoder
import com.kite.model.settings.TrackerSettings
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule(private val app: Application) {

    @Provides
    @Singleton
    fun provideContext(): Context = app

    @Singleton
    @Provides
    fun provideResources(context: Context): Resources = context.resources

    @Singleton
    @Provides
    fun provideGeocoder(context: Context): Geocoder {
        return Geocoder(context)
    }

    @Singleton
    @Provides
    fun provideTrackerService(context: Context): TrackerSettings {
        return TrackerSettings(context)
    }
}