package br.com.bipos.smartposapi.login

import br.com.bipos.smartposapi.auth.PosAuthService
import br.com.bipos.smartposapi.auth.dto.PosAuthResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/pos/auth")
class PosQrAuthController(
    private val posAuthService: PosAuthService
) {

    @PostMapping("/login/qr")
    fun loginQr(
        @RequestBody @Valid request: PosQrLoginRequest,
        httpRequest: HttpServletRequest
    ): PosAuthResponse {
        return posAuthService.loginWithQr(request, httpRequest)
    }
}