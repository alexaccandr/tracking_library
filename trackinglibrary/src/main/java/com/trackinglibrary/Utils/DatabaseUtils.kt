package com.trackinglibrary.Utils

import io.realm.Realm
import java.io.Closeable

internal object DatabaseUtils {
    fun valid(realm: Realm?): Boolean {
        return realm != null && !realm.isClosed
    }

    fun close(realm: Realm?) {
        closeQuietly(realm)
    }

    fun closeQuietly(realm: Realm?, removeListeners: Boolean): Realm? {
        if (realm != null && !realm.isClosed) {
            if (realm.isInTransaction) {
                try {
                    realm.cancelTransaction()
                } catch (e: Exception) {
                    //ignored
                }

            }
            if (removeListeners) {
                try {
                    realm.removeAllChangeListeners()
                } catch (e: Exception) {
                    //ignored
                }

            }
            closeQuietly(realm)
        }
        return null
    }

    fun <C : Closeable> closeQuietly(closeable: C?): C? {
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
}