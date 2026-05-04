package com.mockcat.ui

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

object MockcatUi {
    /**
     * [Intent] to start the Mockcat editor, optionally in a new document/task (default) like Chucker’s separate window.
     */
    @JvmStatic
    @JvmOverloads
    fun createLaunchIntent(
        context: Context,
        newTaskOrDocument: Boolean = true,
    ): Intent = Intent(context, MockcatActivity::class.java).apply {
        if (newTaskOrDocument) {
            // Separate task (like Chucker) but do **not** use FLAG_ACTIVITY_MULTIPLE_TASK — that
            // allows parallel tasks so each tap opens another window. [singleTask] + [taskAffinity]
            // in the manifest brings an existing editor task to the front instead.
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
