package com.android.jerry.ramayanabakery

import android.content.Context
import android.net.ConnectivityManager
import android.widget.Toast

fun Context.isConnectedToInternet(): Boolean {
    val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val activeNetwork = cm.activeNetworkInfo

    return activeNetwork != null && activeNetwork.isConnectedOrConnecting
}

fun Context.showToast(message: String, length: Int) {
    Toast.makeText(this, message, length)
            .show()
}