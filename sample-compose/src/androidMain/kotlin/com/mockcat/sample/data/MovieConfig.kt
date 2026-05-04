package com.mockcat.sample.data

/**
 * Base URL for the in-repo Ktor `sample-server` module.
 * Android emulator: `10.0.2.2` reaches the host loopback. Physical device: `adb reverse tcp:8080 tcp:8080`
 * and `http://127.0.0.1:8080`, or your machine’s LAN address.
 */
object MovieConfig {
    const val BASE_URL: String = "http://10.0.2.2:8080"
}
