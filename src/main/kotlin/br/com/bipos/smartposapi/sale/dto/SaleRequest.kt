package br.com.bipos.smartposapi.sale.dto

data class SaleRequest (
    val items: List<SaleItemRequest>,
    val paymentMethod: PaymentMethod
)