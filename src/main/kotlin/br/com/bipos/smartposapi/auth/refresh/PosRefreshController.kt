package br.com.bipos.smartposapi.auth.refresh

import br.com.bipos.smartposapi.auth.PosJwtService
import br.com.bipos.smartposapi.auth.dto.PosAuthResponse
import br.com.bipos.smartposapi.auth.refresh.dto.RefreshRequest
import br.com.bipos.smartposapi.credential.PosCredentialRepository
import br.com.bipos.smartposapi.exception.InvalidRefreshTokenException
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/pos/auth")
class PosRefreshController(
    private val jwtService: PosJwtService,
    private val refreshService: PosRefreshTokenService,
    private val credentialRepository: PosCredentialRepository
) {

    @PostMapping("/refresh")
    fun refresh(@RequestBody request: RefreshRequest): PosAuthResponse {

        // 1️⃣ valida refresh token (independente de JWT)
        val refresh = refreshService.validate(request.refreshToken)

        // 2️⃣ recupera a credencial POS da company
        val credential = credentialRepository
            .findByCompanyIdAndActiveTrue(refresh.companyId)
            ?: throw InvalidRefreshTokenException()

        // 3️⃣ gera novo access token COMPLETO
        val token = jwtService.generateToken(credential)

        return PosAuthResponse(
            token = token,
            cnpj = credential.cnpj,
            companyId = credential.company.id.toString(),
            companyName = credential.company.name,
            serialNumber = credential.serialNumber,
            posVersion = credential.posVersion,
        )
    }
}

