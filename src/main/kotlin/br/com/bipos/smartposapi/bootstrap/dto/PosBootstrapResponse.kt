package br.com.bipos.smartposapi.bootstrap.dto

import java.util.UUID

data class PosBootstrapResponse(
    val companyId: UUID,
    val companyName: String,
    val modules: List<PosModuleDTO>
)
