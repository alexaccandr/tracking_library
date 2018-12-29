package com.trackinglibrary.Utils

import android.content.Context
import io.realm.Realm
import io.realm.RealmConfiguration
import java.io.Closeable

object DatabaseUtils {

    fun initDatabase(context: Context) {
        Realm.init(context)

        val config = RealmConfiguration.Builder()
            .schemaVersion(1)
            .name("tracker.database").build()
        Realm.setDefaultConfiguration(config)
    }

    fun valid(realm: Realm?): Boolean {
        return realm != null && !realm.isClosed
    }

    fun close(realm: Realm?) {
        closeQuietly(realm)
    }

    private fun <C : Closeable> closeQuietly(closeable: C?): C? {
        if (closeable != null) {
            try {
                closeable.close()
            } catch (e: Exception) {
                //ignored
            }

        }
        return null
    }

    fun executeTransaction(realm: Realm, transaction: Realm.Transaction) {
        realm.executeTransaction(transaction)
    }

    fun openDB(): Realm = Realm.getDefaultInstance()
}