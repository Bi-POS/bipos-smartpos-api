package br.com.bipos.smartposapi.auth

import br.com.bipos.smartposapi.audit.AuditAction
import br.com.bipos.smartposapi.audit.PosAuditService
import br.com.bipos.smartposapi.auth.dto.*
import br.com.bipos.smartposapi.credential.PosDeviceRepository
import br.com.bipos.smartposapi.exception.InvalidPosCredentialsException
import br.com.bipos.smartposapi.user.AppUserRepository
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class PosAuthService(
    private val posDeviceRepository: PosDeviceRepository,
    private val userRepository: AppUserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: PosJwtService,
    private val auditService: PosAuditService
) {

    fun login(
        request: PosAuthRequest,
        httpRequest: HttpServletRequest
    ): PosAuthResponse {

        println("========== LOGIN POS ==========")
        println("EMAIL: ${request.email}")
        println("DOCUMENT: ${request.document}")
        println("SERIAL: ${request.serialNumber}")

        val user = when {
            !request.email.isNullOrBlank() -> {
                userRepository.findByEmailAndActiveTrue(
                    request.email.trim().lowercase()
                )
            }

            !request.document.isNullOrBlank() -> {
                val doc = request.document.replace(Regex("[^0-9]"), "")
                userRepository.findByDocumentAndActiveTrue(doc)
            }

            else -> {
                loginFailed(httpRequest)
            }
        } ?: loginFailed(httpRequest)

        val passwordOk = passwordEncoder.matches(
            request.password,
            user.passwordHash
        )

        if (!passwordOk) loginFailed(httpRequest)

        val pos = posDeviceRepository
            .findBySerialNumberAndActiveTrue(request.serialNumber)
            ?: loginFailed(httpRequest)

        if (pos.company.id != user.company?.id) {
            loginFailed(httpRequest)
        }

        println("✅ LOGIN VALIDATED")

        /* 6️⃣ Atualiza dados do POS */
        pos.lastSeenAt = LocalDateTime.now()
        pos.posVersion = request.posVersion
        posDeviceRepository.save(pos)

        /* 7️⃣ Auditoria */
        auditService.log(
            companyId = pos.company.id,
            action = AuditAction.LOGIN_SUCCESS.name,
            request = httpRequest,
            serialNumber = pos.serialNumber,
            posVersion = pos.posVersion
        )

        /* 8️⃣ Gera token POS + USER */
        val token = jwtService.generateToken(
            user = user,
            pos = pos
        )

        /* 9️⃣ Retorno */
        return PosAuthResponse(
            token = token,

            company = CompanySnapshot(
                id = pos.company.id.toString(),
                name = user.company?.name ?: "",
                cnpj = user.company?.document,
                logoPath = user.company?.logoUrl
            ),

            user = UserSnapshot(
                id = user.id?.toString(),
                name = user.name,
                photoPath = user.photoUrl
            ),

            pos = PosSnapshot(
                serialNumber = pos.serialNumber,
                version = pos.posVersion
            )
        )
    }

    private fun loginFailed(httpRequest: HttpServletRequest): Nothing {
        auditService.log(
            companyId = null,
            action = AuditAction.LOGIN_FAILED.name,
            request = httpRequest
        )
        throw InvalidPosCredentialsException()
    }
}

