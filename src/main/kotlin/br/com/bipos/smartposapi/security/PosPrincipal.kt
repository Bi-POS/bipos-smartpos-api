package br.com.bipos.smartposapi.security

import java.util.UUID

data class PosPrincipal(
    val userId: UUID,
    val companyId: UUID,
    val serialNumber: String
)
