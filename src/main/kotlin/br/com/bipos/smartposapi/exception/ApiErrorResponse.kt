package br.com.bipos.smartposapi.exception

import java.time.Instant

data class ApiErrorResponse(
    val timestamp: Instant,
    val status: Int,
    val error: String,
    val message: String,
    val path: String
)
