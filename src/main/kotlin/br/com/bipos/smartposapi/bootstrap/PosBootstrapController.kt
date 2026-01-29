package br.com.bipos.smartposapi.bootstrap

import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/pos")
class PosBootstrapController {

    @GetMapping("/bootstrap")
    fun bootstrap(authentication: Authentication): Map<String, Any> {
        return mapOf(
            "companyId" to authentication.principal,
            "status" to "OK"
        )
    }
}
