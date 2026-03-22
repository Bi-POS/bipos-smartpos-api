package br.com.bipos.smartposapi.sale

import br.com.bipos.smartposapi.company.CompanyService
import br.com.bipos.smartposapi.exception.ResourceNotFoundException
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
        val user = userRepository.findByIdAndActiveTrue(PosSecurityUtils.userId())
            ?: throw ResourceNotFoundException("Usuário não encontrado")

        val auth = PosSecurityUtils.authContext(user)

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

