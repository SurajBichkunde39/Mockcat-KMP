package com.mockcat.ui

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

object MockcatUi {
    @JvmStatic
    @JvmOverloads
    fun createLaunchIntent(
        context: Context,
        newTaskOrDocument: Boolean = true,
    ): Intent = Intent(context, MockcatActivity::class.java).apply {
        if (newTaskOrDocument) {
            // Do NOT use FLAG_ACTIVITY_MULTIPLE_TASK — that allows parallel tasks so each tap opens
            // another window. [singleTask] + [taskAffinity] in the manifest brings an existing
            // editor task to the front instead.
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    /**
     * For notification-style entry points: same as [createLaunchIntent] with [Activity.getIntent] extras handling left to the host.
     */
    @JvmStatic
    @JvmOverloads
    fun createLaunchPendingIntent(
        context: Context,
        requestCode: Int = 0,
    ): PendingIntent = PendingIntent.getActivity(
        context,
        requestCode,
        createLaunchIntent(context, newTaskOrDocument = true),
        PendingIntent.FLAG_UPDATE_CURRENT or
            (if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0),
    )
}
