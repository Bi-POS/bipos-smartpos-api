package br.com.bipos.smartposapi.stock

import br.com.bipos.smartposapi.domain.catalog.Product
import br.com.bipos.smartposapi.domain.company.Company
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "stock_movements")
class StockMovement(

    @Id
    @GeneratedValue
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    val company: Company,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    val product: Product,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: StockMovementType,

    /**
     * Sempre positivo ou negativo
     * Ex: -2 venda | +10 entrada
     */
    @Column(nullable = false)
    val quantity: Int,

    /**
     * Quantidade FINAL após a movimentação
     * Facilita relatório e auditoria
     */
    @Column(nullable = false)
    val balanceAfter: Int,

    val referenceId: UUID? = null, // saleId, adjustmentId, etc

    val createdAt: LocalDateTime = LocalDateTime.now()
)
