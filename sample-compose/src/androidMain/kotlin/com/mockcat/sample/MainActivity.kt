package com.mockcat.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.mockcat.persistence.getMockcatStoreForAndroid
import com.mockcat.ui.MockcatApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = getMockcatStoreForAndroid(this)
        setContent {
            MockcatApp(store = store)
        }
    }
}
