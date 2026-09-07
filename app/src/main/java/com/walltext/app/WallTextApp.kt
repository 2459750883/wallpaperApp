package com.walltext.app

import android.app.Application
import com.walltext.app.data.AppDatabase

class WallTextApp : Application() {

    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        @Volatile
        lateinit var instance: WallTextApp
            private set
    }
}