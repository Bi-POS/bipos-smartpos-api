package br.com.bipos.smartposapi.sale

data class CartItem(
    val product: Product,
    val quantity: Int
) {
    val subtotal = product.price * quantity.toBigDecimal()
}