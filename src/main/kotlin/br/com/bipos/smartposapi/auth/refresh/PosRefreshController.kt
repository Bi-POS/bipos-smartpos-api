package br.com.bipos.smartposapi.auth.refresh

import br.com.bipos.smartposapi.auth.PosJwtService
import br.com.bipos.smartposapi.auth.dto.CompanySnapshot
import br.com.bipos.smartposapi.auth.dto.PosAuthResponse
import br.com.bipos.smartposapi.auth.dto.PosSnapshot
import br.com.bipos.smartposapi.auth.dto.UserSnapshot
import br.com.bipos.smartposapi.auth.refresh.dto.RefreshRequest
import br.com.bipos.smartposapi.credential.PosDeviceRepository
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
    private val posDeviceRepository: PosDeviceRepository,
    private val userRepository: AppUserRepository
) {

    @PostMapping("/refresh")
    fun refresh(@RequestBody request: RefreshRequest): PosAuthResponse {

        /* 1️⃣ Valida refresh token */
        val refresh = refreshService.validate(request.refreshToken)

        /* 2️⃣ Busca usuário */
        val user = userRepository.findByIdAndActiveTrue(refresh.userId)
            ?: throw InvalidRefreshTokenException()

        /* 3️⃣ Busca POS */
        val pos = posDeviceRepository
            .findBySerialNumberAndActiveTrue(refresh.serialNumber)
            ?: throw InvalidRefreshTokenException()

        /* 4️⃣ Garante consistência */
        if (user.company?.id != refresh.companyId ||
            pos.company.id != refresh.companyId
        ) {
            throw InvalidRefreshTokenException()
        }

        /* 5️⃣ Gera novo access token */
        val token = jwtService.generateToken(
            user = user,
            pos = pos
        )

        return PosAuthResponse(
            token = token,

            company = CompanySnapshot(
                id = pos.company.id.toString(),
                name = user.company?.name ?: "",
                cnpj = user.company?.document,
                logoPath = user.company?.logoUrl,
                email = user.company?.email,
                phone = user.company?.phone
            ),

            user = UserSnapshot(
                id = user.id?.toString(),
                name = user.name,
                photoPath = user.photoUrl,
                email = user.email,
                role = user.role.name,
            ),

            pos = PosSnapshot(
                serialNumber = pos.serialNumber,
                version = pos.posVersion
            )
        )
    }
}
