package com.mockcat.api

import kotlinx.serialization.Serializable

@Serializable
enum class MockType {
    STATIC,
    REDIRECT,
}
