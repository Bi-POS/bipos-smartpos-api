package br.com.bipos.smartposapi.auth.dto

data class PosAuthResponse(
    val token: String,
    val cnpj: String,
    val companyId: String,
    val companyName: String?,
    val serialNumber: String?,
    val posVersion: String?
)
