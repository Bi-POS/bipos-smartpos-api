package br.com.bipos.smartposapi.sale.product

import br.com.bipos.smartposapi.sale.product.dto.PosSaleProductDTO
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/pos/sale/groups")
class PosSaleProductController(
    private val service: PosSaleProductService
) {
    @GetMapping("/products")
    fun listAll(): List<PosSaleProductDTO> =
        service.list()

    @GetMapping("/{groupId}/products")
    fun listByGroup(
        @PathVariable groupId: UUID
    ): List<PosSaleProductDTO> =
        service.listByGroup(groupId)
}