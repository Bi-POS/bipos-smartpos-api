package br.com.bipos.smartposapi.sale.dto

import br.com.bipos.smartposapi.payment.PaymentMethod

data class SaleRequest(
    val items: List<SaleItemRequest>,
    val paymentMethod: PaymentMethod
)