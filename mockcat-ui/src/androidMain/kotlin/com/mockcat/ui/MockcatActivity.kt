package com.mockcat.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.mockcat.persistence.getMockcatStoreForAndroid

class MockcatActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = getMockcatStoreForAndroid(this)
        setContent {
            MockcatApp(store = store)
        }
    }
}
