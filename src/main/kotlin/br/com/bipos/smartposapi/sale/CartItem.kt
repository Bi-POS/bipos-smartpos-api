package br.com.bipos.smartposapi.sale

import br.com.bipos.smartposapi.domain.catalog.Product

data class CartItem(
    val product: Product,
    val quantity: Int
) {
    val subtotal = product.price * quantity.toBigDecimal()
}