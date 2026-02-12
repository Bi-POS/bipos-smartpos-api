package br.com.bipos.smartposapi.domain.catalog

import br.com.bipos.smartposapi.domain.user.AppUser
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

    @ManyToOne(fetch = FetchType.LAZY)
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

    @Column(name = "pos_serial", nullable = false)
    val posSerial: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: AppUser,

    val paidAt: LocalDateTime = LocalDateTime.now(),

    // ✅ CAMPOS DO TEF - AGORA NO PAYMENT!
    @Column(name = "nsu", length = 20)
    var nsu: String? = null,

    @Column(name = "authorization_code", length = 20)
    var authorizationCode: String? = null,

    @Column(name = "card_brand", length = 30)
    var cardBrand: String? = null,

    @Column(name = "card_number_masked", length = 20)
    var cardNumberMasked: String? = null,

    @Column(name = "installments")
    var installments: Int = 1,

    @Column(name = "host_message", length = 50)
    var hostMessage: String? = null,

    @Column(name = "acquirer_response", length = 10)
    var acquirerResponse: String? = null
)