package br.com.bipos.smartposapi.auth.refresh

import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(name = "pos_refresh_tokens")
class PosRefreshToken(

    @Id
    @GeneratedValue
    val id: UUID? = null,

    @Column(nullable = false, unique = true)
    val token: String,

    @Column(nullable = false)
    val userId: UUID,

    @Column(nullable = false)
    val companyId: UUID,

    @Column(name = "serial_number", nullable = false)
    val serialNumber: String,

    @Column(nullable = false)
    var active: Boolean = true,

    @Column(name = "expires_at", nullable = false)
    val expiresAt: Instant
)

