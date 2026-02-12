package br.com.bipos.smartposapi.sale.dto

import br.com.bipos.smartposapi.payment.PaymentMethod
import java.math.BigDecimal

data class SaleRequest(
    val items: List<SaleItemRequest>,
    val paymentMethod: PaymentMethod,
    val amount: BigDecimal,
    val nsu: String? = null,
    val authorizationCode: String? = null,
    val cardBrand: String? = null,
    val cardNumberMasked: String? = null,
    val installments: Int = 1,
    val hostMessage: String? = null,
    val acquirerResponse: String? = null
)