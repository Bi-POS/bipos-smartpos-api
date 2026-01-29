package br.com.bipos.smartposapi.credential

import br.com.bipos.smartposapi.domain.company.Company
import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "pos_credentials", schema = "public")
class PosCredential(

    @Id
    @GeneratedValue
    val id: UUID? = null,

    @Column(nullable = false)
    var cnpj: String,

    @Column(nullable = false)
    var passwordHash: String,

    @Column(nullable = false)
    var active: Boolean = true,

    @Column(name = "serial_number", nullable = true)
    var serialNumber: String? = null,

    @Column(name = "pos_version", nullable = true)
    var posVersion: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    val company: Company
)


