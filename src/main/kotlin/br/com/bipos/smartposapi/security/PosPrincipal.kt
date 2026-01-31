package br.com.bipos.smartposapi.security

data class PosPrincipal(
    val companyId: String,
    val tokenType: String
)
