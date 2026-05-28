package com.dulumina.androtik

import android.app.Application
import com.dulumina.androtik.di.AppContainer

class AndrotikApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
