package br.com.bipos.smartposapi.sale.dto

import java.util.*

data class SaleItemRequest(
    val productId: UUID,
    val quantity: Int
)