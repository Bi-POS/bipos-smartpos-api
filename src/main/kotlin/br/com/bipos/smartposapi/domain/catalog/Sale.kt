package br.com.bipos.smartposapi.domain.catalog

import br.com.bipos.smartposapi.domain.company.Company
import br.com.bipos.smartposapi.sale.SaleStatus
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "sales")
class Sale(

    @Id
    @GeneratedValue
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    val company: Company,

    var totalAmount: BigDecimal,

    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: SaleStatus = SaleStatus.CREATED,

    @OneToMany(
        mappedBy = "sale",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    val items: MutableList<SaleItem> = mutableListOf(),

    @OneToMany(
        mappedBy = "sale",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    val payments: MutableList<Payment> = mutableListOf()
)

