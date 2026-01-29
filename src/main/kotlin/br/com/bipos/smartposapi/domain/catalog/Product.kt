package br.com.bipos.smartposapi.domain.catalog

import jakarta.persistence.*
import java.math.BigDecimal
import java.util.*

@Entity
@Table(name = "sale_products")
class Product(

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false, precision = 10, scale = 2)
    var price: BigDecimal,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var unitType: UnitType,

    @Column(name = "image_url")
    var imageUrl: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    var group: Group
)