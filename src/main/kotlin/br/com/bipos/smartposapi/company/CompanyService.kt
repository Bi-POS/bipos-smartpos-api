package br.com.bipos.smartposapi.company

import br.com.bipos.smartposapi.domain.company.Company
import br.com.bipos.smartposapi.security.PosSecurityUtils
import org.springframework.stereotype.Service

@Service
class CompanyService(
    private val companyRepository: CompanyRepository
) {

    fun getCurrentCompany(): Company {
        val companyId = PosSecurityUtils.companyId()

        return companyRepository.findById(companyId)
            .orElseThrow { RuntimeException("Company não existe") }
    }
}

