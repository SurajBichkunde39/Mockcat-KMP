package com.mockcat.logger.ui

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri

/**
 * Runs at process startup (before [android.app.Application.onCreate]) to wire up the notification
 * observer automatically. Host apps do not need to call anything — just add the dependency.
 *
 * Uses the same ContentProvider auto-init pattern as WorkManager, Firebase, and LeakCanary.
 */
internal class MockcatLoggerUiInitializer : ContentProvider() {
    override fun onCreate(): Boolean {
        context?.let { MockcatLoggerUi.enableNotifications(it) }
        return true
    }

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? = null
    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
}
