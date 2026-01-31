package br.com.bipos.smartposapi.sale

import br.com.bipos.smartposapi.sale.dto.SaleRequest
import br.com.bipos.smartposapi.sale.dto.SaleResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/pos/sales")
class SaleController(
    private val saleService: SaleService
) {

    @PostMapping
    fun createSale(
        @RequestBody request: SaleRequest
    ): ResponseEntity<SaleResponse> {

        val sale = saleService.createSale(request)

        return ResponseEntity.status(HttpStatus.CREATED).body(
            SaleResponse.from(sale)
        )
    }
}
