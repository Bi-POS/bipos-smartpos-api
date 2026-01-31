package br.com.bipos.smartposapi.domain.catalog

import br.com.bipos.smartposapi.payment.PaymentMethod
import br.com.bipos.smartposapi.payment.PaymentStatus
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "payments")
class Payment(

    @Id
    @GeneratedValue
    val id: UUID? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    val sale: Sale,

    @Column(nullable = false, precision = 10, scale = 2)
    val amount: BigDecimal,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val method: PaymentMethod,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: PaymentStatus = PaymentStatus.PAID,

    val paidAt: LocalDateTime = LocalDateTime.now()
)