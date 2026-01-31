package br.com.bipos.smartposapi.company

import br.com.bipos.smartposapi.domain.company.Company
import br.com.bipos.smartposapi.security.PosPrincipal
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class CompanyService(
    private val companyRepository: CompanyRepository
) {
    fun findById(id: UUID?): Company {
        return companyRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Empresa não encontrada") }
    }


    fun getCurrentCompany(): Company {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw RuntimeException("Usuário não autenticado")

        val principal = authentication.principal as PosPrincipal

        val companyId = principal.companyId
            ?: throw RuntimeException("Company não encontrada no token")

        return companyRepository.findById(companyId)
            .orElseThrow { RuntimeException("Company não existe") }
    }
}
