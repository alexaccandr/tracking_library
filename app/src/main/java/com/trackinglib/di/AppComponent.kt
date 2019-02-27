package com.trackinglib.di

import com.trackinglib.presenter.MapPresenter
import com.trackinglib.presenter.StartTrackerPresenter
import com.trackinglib.presenter.TracksListPresenter
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class
    ]
)
interface AppComponent {
    fun inject(presenter: TracksListPresenter)
    fun inject(presenter: MapPresenter)
    fun inject(presenter: StartTrackerPresenter)
}