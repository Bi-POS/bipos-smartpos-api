package br.com.bipos.smartposapi.sale.dto

import br.com.bipos.smartposapi.sale.SaleStatus
import java.math.BigDecimal
import java.util.*

data class SaleResponse(
    val id: UUID?,
    val totalAmount: BigDecimal,
    val status: SaleStatus
)
