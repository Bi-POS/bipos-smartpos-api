package br.com.bipos.smartposapi.domain.stock

import br.com.bipos.smartposapi.domain.catalog.Product
import br.com.bipos.smartposapi.domain.company.Company
import jakarta.persistence.*
import java.util.*

@Entity
@Table(
    name = "stocks",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["company_id", "product_id"])
    ]
)
class Stock(

    @Id
    @GeneratedValue
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    val company: Company,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    val product: Product,

    @Column(nullable = false)
    var quantity: Int
)
