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

    @Column(nullable = false)
    val companyId: UUID,

    @Column(nullable = false, unique = true, length = 64)
    val token: String,

    @Column(nullable = false)
    val expiresAt: Instant,

    @Column(nullable = false)
    var active: Boolean = true,

    @Column(nullable = false)
    val createdAt: Instant = Instant.now()
)
