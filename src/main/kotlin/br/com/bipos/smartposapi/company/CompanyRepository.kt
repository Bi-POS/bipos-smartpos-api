package br.com.bipos.smartposapi.company

import br.com.bipos.smartposapi.domain.company.Company
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface CompanyRepository : JpaRepository<Company, String>
