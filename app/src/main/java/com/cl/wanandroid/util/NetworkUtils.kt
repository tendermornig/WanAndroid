package com.cl.wanandroid.util

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Build
import android.provider.Settings
import com.cl.wanandroid.WanAndroidApplication

fun isNetworkConnect(): Boolean {
    val cm =
        WanAndroidApplication.context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
        cm.activeNetworkInfo?.isAvailable ?: false
    } else {
        cm.getNetworkCapabilities(cm.activeNetwork) != null
    }
}

fun toNetworkSetting(context: Context) {
    val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
    context.startActivity(intent)
}