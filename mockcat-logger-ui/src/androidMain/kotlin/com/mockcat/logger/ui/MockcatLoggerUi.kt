package com.mockcat.logger.ui

import android.content.Context
import android.content.Intent

object MockcatLoggerUi {
    @JvmStatic
    @JvmOverloads
    fun createLaunchIntent(
        context: Context,
        newTaskOrDocument: Boolean = true,
    ): Intent = Intent(context, HttpLogListActivity::class.java).apply {
        if (newTaskOrDocument) {
            // Same pattern as [mockcat-intercept-ui.MockcatUi]: new task without FLAG_ACTIVITY_MULTIPLE_TASK
            // so [singleTask] + [taskAffinity] in the manifest reuses one log window.
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    /**
     * @return an [Intent] to show the full-screen HTTP log (same as [createLaunchIntent]).
     * Named for parity with a “get screen / intent” Chucker-style entry point; it is not a Composable.
     */
    @JvmStatic
    @JvmName("getHttpLogListScreen")
    fun getHttpLogListScreen(
        context: Context,
    ): Intent = createLaunchIntent(context, newTaskOrDocument = true)
}
