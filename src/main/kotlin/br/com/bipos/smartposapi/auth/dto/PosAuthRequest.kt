package br.com.bipos.smartposapi.auth.dto

data class PosAuthRequest(
    val document: String,
    val password: String,
    val serialNumber: String,
    val posVersion: String
)
