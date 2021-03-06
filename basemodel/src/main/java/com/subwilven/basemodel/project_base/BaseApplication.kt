package com.subwilven.basemodel.project_base

import android.annotation.SuppressLint
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.FragmentManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.subwilven.basemodel.project_base.base.boradcast.AlerterReceiver
import com.subwilven.basemodel.project_base.base.boradcast.ConnectivityReceiver
import com.subwilven.basemodel.project_base.utils.LocalManager
import com.subwilven.basemodel.project_base.utils.NotificationManager
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.module


public open class BaseApplication : Application() {


    companion object {
        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }

        @SuppressLint("StaticFieldLeak")
        @get:Synchronized
        var instance: BaseApplication? = null
            private set

    }



    internal var connectivityReceivern: BroadcastReceiver? = null
    internal var alerterReceiver: BroadcastReceiver? = null

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocalManager.updateResources(base, LocalManager.getLanguage(base)))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        LocalManager.setLocale(this)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        initConnectivityBroadcast()
        initAlerterBroadcast()
        NotificationManager.initNotificationChannels(this)

    }

    private fun initAlerterBroadcast() {
        alerterReceiver = AlerterReceiver()
        val filter = IntentFilter("action_alert")
        LocalBroadcastManager.getInstance(this).registerReceiver(alerterReceiver!!, filter)
    }

    private fun initConnectivityBroadcast() {
        connectivityReceivern = ConnectivityReceiver()
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(connectivityReceivern, filter)
    }

    fun showAlertBar(message: com.subwilven.basemodel.project_base.POJO.Message){
        val intent = Intent("action_alert")
        intent.putExtra("message", message.getValue(this))
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
    fun setConnectivityListener(listener: ConnectivityReceiver.ConnectivityReceiverListener?) {
        ConnectivityReceiver.listener = listener
    }
    fun setAlerterListener(listener: AlerterReceiver.AlerterReceiverListener?) {
        AlerterReceiver.listener = listener
    }
    override fun onTerminate() {
        unregisterReceiver(connectivityReceivern)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(alerterReceiver!!)
        super.onTerminate()
    }
}

