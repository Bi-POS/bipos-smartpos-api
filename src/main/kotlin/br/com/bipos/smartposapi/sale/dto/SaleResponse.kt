package br.com.bipos.smartposapi.sale.dto

import br.com.bipos.smartposapi.domain.catalog.Sale
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class SaleResponse(
    val saleId: UUID,
    val totalAmount: BigDecimal,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(sale: Sale) = SaleResponse(
            saleId = sale.id!!,
            totalAmount = sale.totalAmount,
            createdAt = sale.createdAt
        )
    }
}
