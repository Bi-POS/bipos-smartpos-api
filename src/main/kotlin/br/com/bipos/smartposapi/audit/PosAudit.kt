package br.com.bipos.smartposapi.audit

import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(name = "pos_audit")
class PosAudit(

    @Id
    @GeneratedValue
    val id: UUID? = null,

    @Column(nullable = true)
    val companyId: UUID?,

    @Column(nullable = false)
    val action: String, // LOGIN, SALE, SYNC, etc

    @Column(nullable = false)
    val ipAddress: String,

    @Column(nullable = true)
    val serialNumber: String? = null,

    @Column(nullable = true)
    val posVersion: String? = null,

    @Column(nullable = false)
    val createdAt: Instant = Instant.now()
)
