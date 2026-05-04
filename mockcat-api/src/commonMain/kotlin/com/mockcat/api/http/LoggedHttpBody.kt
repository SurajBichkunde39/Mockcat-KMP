package com.mockcat.api.http

import kotlinx.serialization.Serializable

/**
 * Request or response body for logging (bounded, non-streaming). For binary payloads, adapters may
 * use [Text] with a charset or [Omitted] if not read.
 */
@Serializable
sealed class LoggedHttpBody {
    @Serializable
    data object None : LoggedHttpBody()

    @Serializable
    data class Text(
        val text: String,
        val contentType: String? = null,
        /** Size in bytes if known (e.g. Content-Length or buffer size). */
        val byteLength: Long? = null,
    ) : LoggedHttpBody()

    @Serializable
    data class Omitted(
        val reason: OmissionReason,
    ) : LoggedHttpBody()
}

@Serializable
enum class OmissionReason {
    /** Body exceeded the configured read limit. */
    TOO_LARGE,

    /** Intentionally not read (e.g. performance). */
    NOT_READ,

    /** No body expected (e.g. HEAD). */
    NOT_APPLICABLE,
}
