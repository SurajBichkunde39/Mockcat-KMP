package com.mockcat.logger.ui

import com.mockcat.api.http.HttpHeaderField
import com.mockcat.api.http.HttpRequestSnapshot
import com.mockcat.api.http.LoggedHttpBody

internal fun buildCurlCommand(request: HttpRequestSnapshot): String = buildString {
    append("curl -X ")
    append(request.method)

    var compressed = false
    for (header in request.headers) {
        if (isCompressedEncoding(header)) compressed = true
        append(" -H \"")
        append(header.name)
        append(": ")
        append(header.value.replace("\"", "\\\""))
        append("\"")
    }

    val bodyText = (request.body as? LoggedHttpBody.Text)
        ?.text
        ?.takeIf { it.isNotEmpty() }

    if (bodyText != null) {
        // ANSI-C quoting ($'...') lets us embed newlines as \n in a single-line command.
        // Escape order matters: backslash first, then single-quote, then control characters.
        val escaped = bodyText
            .replace("\\", "\\\\")
            .replace("'", "\\'")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
        append(" --data $'")
        append(escaped)
        append("'")
    }

    if (compressed) append(" --compressed") else append(" ")
    append(request.url.replace(" ", "%20"))
}

private fun isCompressedEncoding(header: HttpHeaderField): Boolean = header.name.equals("Accept-Encoding", ignoreCase = true) &&
    (
        header.value.contains("gzip", ignoreCase = true) ||
            header.value.contains("br", ignoreCase = true)
        )
