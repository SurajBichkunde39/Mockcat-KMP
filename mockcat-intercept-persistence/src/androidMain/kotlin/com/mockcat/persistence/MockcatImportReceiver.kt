package com.mockcat.persistence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File

private const val TAG = "MockcatImportReceiver"
private const val ACTION_IMPORT_MOCKS = "com.mockcat.action.IMPORT_MOCKS"
private const val EXTRA_FILE_PATH = "mock_file_path"

class MockcatImportReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive: action=${intent.action}")
        if (intent.action != ACTION_IMPORT_MOCKS) return

        val path = intent.getStringExtra(EXTRA_FILE_PATH) ?: run {
            Log.w(TAG, "Missing $EXTRA_FILE_PATH extra")
            return
        }
        Log.d(TAG, "Reading mock file: $path")
        // Read synchronously here — `am broadcast` is synchronous and the Gradle task only
        // deletes the remote file AFTER onReceive returns, so this read is race-free.
        val content = try {
            File(path).readText(Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read $path", e)
            return
        }
        Log.d(TAG, "Read ${content.length} bytes, starting import")

        val pending = goAsync()
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                val count = ProcessMockcatStore.get(context).importFromJsonReplaceAll(content)
                Log.d(TAG, "Import complete: $count entries")
            } catch (e: Exception) {
                Log.e(TAG, "Import failed", e)
            } finally {
                pending.finish()
            }
        }
    }
}
