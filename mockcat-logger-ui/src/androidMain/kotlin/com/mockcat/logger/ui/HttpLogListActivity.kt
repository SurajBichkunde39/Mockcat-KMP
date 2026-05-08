package com.mockcat.logger.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class HttpLogListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HttpLogListContentFromRegistry(
                onShareCurl = { curlCommand ->
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, curlCommand)
                        putExtra(Intent.EXTRA_SUBJECT, "curl command")
                    }
                    startActivity(Intent.createChooser(intent, "Share curl command"))
                },
            )
        }
    }
}
