package com.mockcat.logger.ui

import android.content.Context
import android.content.Intent
import com.mockcat.logger.HttpLogReaderRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

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
     */
    @JvmStatic
    @JvmName("getHttpLogListScreen")
    fun getHttpLogListScreen(
        context: Context,
    ): Intent = createLaunchIntent(context, newTaskOrDocument = true)

    private var notificationsEnabled = false

    /**
     * Called automatically by [MockcatLoggerUiInitializer] at process startup.
     * Idempotent — safe to call multiple times.
     */
    internal fun enableNotifications(context: Context) {
        if (notificationsEnabled) return
        notificationsEnabled = true
        HttpLogNotificationHelper.createChannel(context)
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            // Suspends until the OkHttp/Ktor interceptor installs the reader (happens on first use).
            val reader = HttpLogReaderRegistry.currentFlow.filterNotNull().first()
            reader.observeLogs().collect { calls ->
                if (calls.isNotEmpty() && !HttpLogListActivity.isInForeground) {
                    HttpLogNotificationHelper.show(appContext, calls)
                }
            }
        }
    }
}
