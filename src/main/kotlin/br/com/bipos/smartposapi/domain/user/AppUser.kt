package br.com.bipos.smartposapi.domain.user


import br.com.bipos.smartposapi.domain.company.Company
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(
    name = "users",
    indexes = [
        Index(name = "idx_users_document", columnList = "document")
    ]
)
class AppUser(

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: UUID? = null,

    @Column(nullable = false)
    var name: String = "",

    @Column(nullable = false, unique = true)
    var email: String = "",

    /**
     * CPF ou CNPJ (somente números)
     * Pode ser null para usuários criados só com email
     */
    @Column(nullable = true)
    var document: String? = null,

    @Column(nullable = false)
    var passwordHash: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: UserRole = UserRole.OPERATOR,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    var company: Company? = null,

    @Column(nullable = false)
    var active: Boolean = true,

    var photoUrl: String? = null,

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
)
