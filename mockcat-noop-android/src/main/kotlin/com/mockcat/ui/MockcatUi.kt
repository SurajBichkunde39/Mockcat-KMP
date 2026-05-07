package com.mockcat.ui

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

object MockcatUi {
    @JvmStatic
    @JvmOverloads
    fun createLaunchIntent(
        @Suppress("UnusedPrivateParameter")
        context: Context,
        @Suppress("UnusedPrivateParameter")
        newTaskOrDocument: Boolean = true,
    ): Intent = Intent()

    @JvmStatic
    @JvmOverloads
    fun createLaunchPendingIntent(
        context: Context,
        requestCode: Int = 0,
    ): PendingIntent = PendingIntent.getActivity(
        context,
        requestCode,
        Intent(),
        PendingIntent.FLAG_UPDATE_CURRENT or
            (if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0),
    )
}
