package br.com.bipos.smartposapi.auth.dto

data class PosAuthRequest(
    val email: String? = null,
    val document: String? = null,
    val password: String,
    val serialNumber: String,
    val posVersion: String
)
