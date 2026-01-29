package br.com.bipos.smartposapi.domain.companymodule

import br.com.bipos.smartposapi.domain.company.Company
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.UUID
import br.com.bipos.smartposapi.domain.module.Module

@Entity
@Table(name = "company_modules")
class CompanyModule(

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    val company: Company? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "module_id", nullable = false)
    val module: Module? = null
)