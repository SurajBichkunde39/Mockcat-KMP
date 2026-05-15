package com.mockcat.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.mockcat.api.MockcatStore
import com.mockcat.persistence.ProcessMockcatStore

class MockcatActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store: MockcatStore = ProcessMockcatStore.get(this)
        setContent {
            MockcatApp(store = store)
        }
    }
}
