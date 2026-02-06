package br.com.bipos.smartposapi.credential

import br.com.bipos.smartposapi.domain.company.Company
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "pos_devices")
class PosDevice(

    @Id
    @GeneratedValue
    val id: UUID? = null,

    @Column(name = "serial_number", nullable = false, unique = true)
    val serialNumber: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    val company: Company,

    @Column(nullable = false)
    var active: Boolean = true,

    @Column(name = "provisioned_at")
    var provisionedAt: LocalDateTime? = null,

    @Column(name = "last_seen_at")
    var lastSeenAt: LocalDateTime? = null,

    @Column(name = "pos_version")
    var posVersion: String? = null
)

