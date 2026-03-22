package br.com.bipos.smartposapi.bootstrap

import br.com.bipos.smartposapi.company.CompanyRepository
import br.com.bipos.smartposapi.domain.company.Company
import br.com.bipos.smartposapi.domain.company.CompanyStatus
import br.com.bipos.smartposapi.domain.companymodule.CompanyModule
import br.com.bipos.smartposapi.domain.module.Module
import br.com.bipos.smartposapi.domain.module.ModuleType
import br.com.bipos.smartposapi.domain.utils.DocumentType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Optional
import java.util.UUID

class PosBootstrapServiceTest {
    private val companyRepository: CompanyRepository = mock()
    private val service = PosBootstrapService(companyRepository)

    @Test
    fun `bootstrap reflects authenticated company data`() {
        val company = company().also { currentCompany ->
            currentCompany.modules += CompanyModule(
                company = currentCompany,
                module = Module(name = ModuleType.SCHOOL)
            )
            currentCompany.modules += CompanyModule(
                company = currentCompany,
                module = Module(name = ModuleType.SALE)
            )
        }

        whenever(companyRepository.findById(COMPANY_ID)).thenReturn(Optional.of(company))

        val response = service.bootstrap(
            companyId = COMPANY_ID,
            serialNumber = SERIAL_NUMBER
        )

        assertThat(response.companyId).isEqualTo(COMPANY_ID)
        assertThat(response.companyName).isEqualTo("Bipos")
        assertThat(response.logoUrl).isEqualTo("https://cdn.bipos.com/logo.png")
        assertThat(response.stockEnabled).isTrue()
        assertThat(response.serialNumber).isEqualTo(SERIAL_NUMBER)
        assertThat(response.modules.map { it.code }).containsExactly("SALE", "SCHOOL")
        assertThat(response.modules.map { it.name }).containsExactly("Vendas", "Escola")
    }

    private fun company() = Company(
        id = COMPANY_ID,
        name = "Bipos",
        email = "contato@bipos.com.br",
        document = "12345678000190",
        documentType = DocumentType.CNPJ,
        phone = "71999999999",
        status = CompanyStatus.ACTIVE,
        logoUrl = "https://cdn.bipos.com/logo.png",
        stockEnabled = true
    )

    companion object {
        private val COMPANY_ID: UUID = UUID.fromString("6be3bbb2-622b-4112-8613-92de732331fa")
        private const val SERIAL_NUMBER = "POS001"
    }
}
