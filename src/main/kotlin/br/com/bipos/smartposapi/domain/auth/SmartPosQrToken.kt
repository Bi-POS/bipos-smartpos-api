package br.com.bipos.smartposapi.domain.auth

import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(name = "smartpos_qr_tokens")
class SmartPosQrToken(

    @Id
    @GeneratedValue
    val id: UUID? = null,

    @Column(nullable = false, unique = true)
    val token: String,

    @Column(nullable = false)
    val userId: UUID,

    @Column(nullable = false)
    val companyId: UUID,

    @Column(nullable = false)
    val expiresAt: Instant,

    @Column(nullable = false)
    var used: Boolean = false
)