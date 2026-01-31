package br.com.bipos.smartposapi.domain.catalog

import br.com.bipos.smartposapi.domain.company.Company
import com.fasterxml.jackson.annotation.JsonManagedReference
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

    @OneToMany(mappedBy = "sale", cascade = [CascadeType.ALL], orphanRemoval = true)
    @JsonManagedReference
    val items: MutableList<SaleItem> = mutableListOf()
)
