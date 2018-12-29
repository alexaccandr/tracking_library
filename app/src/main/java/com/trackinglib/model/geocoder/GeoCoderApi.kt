package com.trackinglib.model.geocoder

import android.location.Address
import android.location.Geocoder
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers


object GeoCoderApi {

    fun execute(geocoder: Geocoder, lat: Double, lon: Double, block: ((Address) -> Unit)): Disposable {

        return Observable.defer {
            Observable.just(geocoder.getFromLocation(lat, lon, 1))
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribe(
                { it ->
                    if (it != null && !it.isEmpty()) {
                        block(it[0])
                    }
                },
                { er ->
                    // ignored
                })
    }
}