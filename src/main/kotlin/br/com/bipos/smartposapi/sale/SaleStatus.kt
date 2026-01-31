package br.com.bipos.smartposapi.sale


enum class SaleStatus {

    /** Venda criada mas ainda não finalizada */
    CREATED,

    /** Pagamento iniciado (PIX aguardando, cartão em processamento) */
    PENDING_PAYMENT,

    /** Venda paga com sucesso */
    COMPLETED,

    /** Venda cancelada antes de concluir */
    CANCELLED,

    /** Pagamento falhou */
    FAILED
}