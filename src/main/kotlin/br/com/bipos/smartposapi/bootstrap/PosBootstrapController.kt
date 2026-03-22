package br.com.bipos.smartposapi.bootstrap

import br.com.bipos.smartposapi.bootstrap.dto.PosBootstrapResponse
import br.com.bipos.smartposapi.security.PosSecurityUtils
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/pos")
class PosBootstrapController(
    private val bootstrapService: PosBootstrapService
) {

    @GetMapping("/bootstrap")
    fun bootstrap(): PosBootstrapResponse {
        val principal = PosSecurityUtils.principal()
        return bootstrapService.bootstrap(
            companyId = principal.companyId,
            serialNumber = principal.serialNumber
        )
    }
}
