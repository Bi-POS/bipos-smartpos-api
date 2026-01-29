package br.com.bipos.smartposapi.sale.group

import br.com.bipos.smartposapi.sale.group.dto.PosSaleGroupDTO
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/pos/sale/groups")
class PosSaleGroupController(
    private val service: PosSaleGroupService
) {

    @GetMapping
    fun list(): List<PosSaleGroupDTO> =
        service.list()
}