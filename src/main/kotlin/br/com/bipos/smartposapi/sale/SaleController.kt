package br.com.bipos.smartposapi.sale

import br.com.bipos.smartposapi.auth.PosAuthContext
import br.com.bipos.smartposapi.company.CompanyService
import br.com.bipos.smartposapi.sale.dto.SaleRequest
import br.com.bipos.smartposapi.sale.dto.SaleResponse
import br.com.bipos.smartposapi.security.PosSecurityUtils
import br.com.bipos.smartposapi.user.AppUserRepository
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/pos/sales")
class SaleController(
    private val saleService: SaleService,
    private val companyService: CompanyService,
    private val userRepository: AppUserRepository
) {

    @PostMapping
    fun createSale(
        @RequestBody request: SaleRequest
    ): SaleResponse {

        val principal = PosSecurityUtils.principal()

        val user = userRepository.findByIdAndActiveTrue(principal.userId)
            ?: throw IllegalArgumentException("Usuário inválido")

        val auth = PosAuthContext(
            user = user,
            companyId = principal.companyId,
            serialNumber = principal.serialNumber
        )

        val company = companyService.getCurrentCompany()

        val sale = saleService.createSale(
            auth = auth,
            company = company,
            request = request
        )

        return SaleResponse(
            id = sale.id.toString(),
            totalAmount = sale.totalAmount,
            status = sale.status
        )
    }
}

