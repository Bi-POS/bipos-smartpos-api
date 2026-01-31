package br.com.bipos.smartposapi.stock

enum class StockMovementType {
    SALE,        // venda POS
    PURCHASE,    // entrada
    ADJUSTMENT,  // ajuste manual
    RETURN       // estorno / devolução
}
