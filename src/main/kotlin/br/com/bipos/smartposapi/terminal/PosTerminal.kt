package br.com.bipos.smartposapi.terminal

import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(name = "pos_terminals")
class PosTerminal(

    @Id
    @GeneratedValue
    val id: UUID? = null,

    @Column(nullable = false)
    val companyId: UUID,

    @Column(nullable = false, unique = true)
    val serialNumber: String, // ID físico do terminal

    @Column(nullable = false)
    val model: String,

    @Column(nullable = false)
    var active: Boolean = true,

    @Column(nullable = false)
    val createdAt: Instant = Instant.now()
)
