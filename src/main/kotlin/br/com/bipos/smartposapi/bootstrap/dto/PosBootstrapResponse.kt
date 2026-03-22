package br.com.bipos.smartposapi.bootstrap.dto

import java.util.UUID

data class PosBootstrapResponse(
    val companyId: UUID,
    val companyName: String,
    val logoUrl: String?,
    val stockEnabled: Boolean,
    val serialNumber: String,
    val saleOperationMode: String,
    val modules: List<PosModuleDTO>
)
