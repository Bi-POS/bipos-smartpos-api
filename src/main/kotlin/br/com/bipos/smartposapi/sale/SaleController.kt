package br.com.bipos.smartposapi.sale

import br.com.bipos.smartposapi.company.CompanyService
import br.com.bipos.smartposapi.domain.catalog.Sale
import br.com.bipos.smartposapi.sale.dto.SaleRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/pos/sales")
class SaleController(
    private val saleService: SaleService,
    private val companyService: CompanyService
) {

    @PostMapping
    fun createSale(
        @RequestBody request: SaleRequest
    ): Sale {
        val company = companyService.getCurrentCompany()

        return saleService.createSale(
            companyId = company.id,
            request = request
        )
    }
}
