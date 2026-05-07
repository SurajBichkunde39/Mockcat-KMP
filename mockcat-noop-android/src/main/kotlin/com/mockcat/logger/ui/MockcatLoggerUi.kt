package com.mockcat.logger.ui

import android.content.Context
import android.content.Intent

object MockcatLoggerUi {
    @JvmStatic
    @JvmOverloads
    fun createLaunchIntent(
        @Suppress("UnusedPrivateParameter")
        context: Context,
        @Suppress("UnusedPrivateParameter")
        newTaskOrDocument: Boolean = true,
    ): Intent = Intent()

    @JvmStatic
    @JvmName("getHttpLogListScreen")
    fun getHttpLogListScreen(
        @Suppress("UnusedPrivateParameter")
        context: Context,
    ): Intent = Intent()
}
