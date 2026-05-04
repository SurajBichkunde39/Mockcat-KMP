package com.mockcat.logger.ui

import com.mockcat.logger.persistence.getHttpLogStoreForIos

/**
 * Creates the process-wide Room HTTP log store and registers it on [com.mockcat.logger.HttpLogReaderRegistry].
 * Call once at app startup (e.g. from Swift `onAppear` or `App.init`) before presenting [createHttpLogListViewController].
 */
fun installHttpLogReaderForIos() {
    getHttpLogStoreForIos()
}
