package br.com.bipos.smartposapi.sale

import br.com.bipos.smartposapi.company.CompanyService
import br.com.bipos.smartposapi.sale.dto.SaleRequest
import br.com.bipos.smartposapi.sale.dto.SaleResponse
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
    ): SaleResponse {

        val company = companyService.getCurrentCompany()

        val sale = saleService.createSale(
            companyId = company.id,
            request = request
        )

        return SaleResponse(
            id = sale.id.toString(),
            totalAmount = sale.totalAmount,
            status = sale.status
        )
    }
}