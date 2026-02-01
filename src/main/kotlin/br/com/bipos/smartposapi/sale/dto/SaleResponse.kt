package br.com.bipos.smartposapi.sale.dto

import br.com.bipos.smartposapi.sale.SaleStatus
import java.math.BigDecimal

data class SaleResponse(
    val id: String?,
    val totalAmount: BigDecimal,
    val status: SaleStatus
)
