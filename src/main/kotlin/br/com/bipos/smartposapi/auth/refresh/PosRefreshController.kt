package br.com.bipos.smartposapi.auth.refresh

import br.com.bipos.smartposapi.auth.PosJwtService
import br.com.bipos.smartposapi.auth.dto.CompanySnapshot
import br.com.bipos.smartposapi.auth.dto.PosAuthResponse
import br.com.bipos.smartposapi.auth.dto.PosSnapshot
import br.com.bipos.smartposapi.auth.dto.UserSnapshot
import br.com.bipos.smartposapi.auth.refresh.dto.RefreshRequest
import br.com.bipos.smartposapi.credential.PosCredentialRepository
import br.com.bipos.smartposapi.domain.user.UserRole
import br.com.bipos.smartposapi.exception.InvalidRefreshTokenException
import br.com.bipos.smartposapi.user.AppUserRepository
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/pos/auth")
class PosRefreshController(
    private val jwtService: PosJwtService,
    private val refreshService: PosRefreshTokenService,
    private val credentialRepository: PosCredentialRepository,
    private val userRepository: AppUserRepository
) {

    @PostMapping("/refresh")
    fun refresh(@RequestBody request: RefreshRequest): PosAuthResponse {

        val refresh = refreshService.validate(request.refreshToken)

        val credential = credentialRepository
            .findByCompanyIdAndActiveTrue(refresh.companyId)
            ?: throw InvalidRefreshTokenException()

        val token = jwtService.generateToken(credential)
        val company = credential.company

        val owner = userRepository.findFirstByCompanyIdAndRoleAndActiveTrue(
            company.id!!,
            UserRole.OWNER
        )

        return PosAuthResponse(
            token = token,

            company = CompanySnapshot(
                id = company.id.toString(),
                name = company.name,
                cnpj = company.document,
                logoPath = company.logoUrl
            ),

            user = UserSnapshot(
                id = owner?.id?.toString(),
                name = owner?.name ?: company.name,
                photoPath = owner?.photoUrl
            ),

            pos = PosSnapshot(
                serialNumber = credential.serialNumber,
                version = credential.posVersion
            )
        )
    }
}

