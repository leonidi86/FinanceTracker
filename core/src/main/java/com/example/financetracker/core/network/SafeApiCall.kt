package com.example.financetracker.core.network

import kotlin.coroutines.cancellation.CancellationException

/**
 * Выполняет [call] и оборачивает результат в [Result].
 * [CancellationException] пробрасывается дальше, чтобы не ломать отмену корутин.
 */
suspend fun <T> safeApiCall(call: suspend () -> T): Result<T> =
    try {
        Result.success(call())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
