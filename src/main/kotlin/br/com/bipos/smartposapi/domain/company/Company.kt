package br.com.bipos.smartposapi.domain.company

import br.com.bipos.smartposapi.domain.companymodule.CompanyModule
import br.com.bipos.smartposapi.domain.utils.DocumentType
import jakarta.persistence.*
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import java.util.*

@Entity
@Table(name = "companies")
data class Company(

    @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: UUID? = null,

    @Column(nullable = false) @NotBlank(message = "O nome da empresa é obrigatório.") var name: String = "",

    @Column(nullable = false, unique = true) @Email(message = "E-mail inválido.") var email: String = "",

    @Column(nullable = false, unique = true, length = 20) @Pattern(
        regexp = "^[0-9A-Za-z]+$",
        message = "O CNPJ deve conter apenas caracteres alfanuméricos."
    ) var document: String = "",

    @Enumerated(EnumType.STRING) @Column(nullable = false) var documentType: DocumentType = DocumentType.CNPJ,

    @Column(nullable = false, length = 20) var phone: String = "",

    @Enumerated(EnumType.STRING) @Column(nullable = false) var status: CompanyStatus = CompanyStatus.ACTIVE,


    @Column(nullable = true)
    var logoUrl: String? = null,

    @Column(nullable = false)
    var stockEnabled: Boolean = true,

    @OneToMany(
        mappedBy = "company",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    val modules: MutableList<CompanyModule> = mutableListOf()
)
