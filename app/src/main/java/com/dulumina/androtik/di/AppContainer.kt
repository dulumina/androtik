package com.dulumina.androtik.di

import android.content.Context
import com.dulumina.androtik.data.api.SessionManager
import com.dulumina.androtik.data.local.AndrotikDatabase
import com.dulumina.androtik.data.repository.RouterRepositoryImpl
import com.dulumina.androtik.domain.repository.RouterRepository

class AppContainer(context: Context) {
    private val database = AndrotikDatabase.getInstance(context)
    private val routerProfileDao = database.routerProfileDao()

    val routerRepository: RouterRepository = RouterRepositoryImpl(routerProfileDao)
    val sessionManager = SessionManager()
}
