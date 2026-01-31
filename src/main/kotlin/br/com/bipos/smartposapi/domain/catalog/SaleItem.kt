package br.com.bipos.smartposapi.domain.catalog

import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.*
import java.math.BigDecimal
import java.util.*

@Entity
@Table(name = "sale_items")
class SaleItem(

    @Id
    @GeneratedValue
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id")
    @JsonBackReference
    val sale: Sale,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    val product: Product,

    val quantity: Int,

    val unitPrice: BigDecimal,

    val subtotal: BigDecimal
)
