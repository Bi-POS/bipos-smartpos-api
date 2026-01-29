package br.com.bipos.smartposapi.auth

import br.com.bipos.smartposapi.auth.dto.PosAuthRequest
import br.com.bipos.smartposapi.auth.dto.PosAuthResponse
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/pos/auth")
class PosAuthController(
    private val service: PosAuthService
) {

    @PostMapping("/login")
    fun login(
        @RequestBody request: PosAuthRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<PosAuthResponse> {

        val response = service.login(
            request = request,
            httpRequest = httpRequest
        )

        return ResponseEntity.ok(response)
    }
}
