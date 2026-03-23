package br.com.bipos.smartposapi.bootstrap

import br.com.bipos.smartposapi.bootstrap.dto.PosBootstrapResponse
import br.com.bipos.smartposapi.bootstrap.dto.PosModuleDTO
import br.com.bipos.smartposapi.company.CompanyRepository
import br.com.bipos.smartposapi.domain.module.ModuleType
import br.com.bipos.smartposapi.exception.ResourceNotFoundException
import br.com.bipos.smartposapi.settings.SmartPosSettingsService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class PosBootstrapService(
    private val companyRepository: CompanyRepository,
    private val settingsService: SmartPosSettingsService
) {

    @Transactional(readOnly = true)
    fun bootstrap(
        companyId: UUID,
        serialNumber: String
    ): PosBootstrapResponse {
        val company = companyRepository.findById(companyId)
            .orElseThrow { ResourceNotFoundException("Empresa não encontrada") }

        val saleOperationMode = settingsService.resolveOperationMode(companyId).name

        return PosBootstrapResponse(
            companyId = companyId,
            companyName = company.name,
            logoUrl = company.logoUrl,
            stockEnabled = company.stockEnabled,
            serialNumber = serialNumber,
            saleOperationMode = saleOperationMode,
            modules = company.modules
                .mapNotNull { it.module?.name }
                .distinct()
                .sortedBy { it.name }
                .map { moduleType ->
                    PosModuleDTO(
                        code = moduleType.name,
                        name = moduleType.displayName()
                    )
                }
        )
    }

    private fun ModuleType.displayName(): String =
        when (this) {
            ModuleType.SALE -> "Vendas"
            ModuleType.SCHOOL -> "Escola"
        }
}
