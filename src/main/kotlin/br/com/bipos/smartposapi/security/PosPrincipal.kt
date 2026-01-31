package br.com.bipos.smartposapi.security

import java.util.UUID

data class PosPrincipal(
    val companyId: UUID,
    val tokenType: String
)
