package br.com.bipos.smartposapi.auth

import br.com.bipos.smartposapi.audit.AuditAction
import br.com.bipos.smartposapi.audit.PosAuditService
import br.com.bipos.smartposapi.auth.dto.*
import br.com.bipos.smartposapi.credential.PosCredentialRepository
import br.com.bipos.smartposapi.domain.user.UserRole
import br.com.bipos.smartposapi.exception.InvalidPosCredentialsException
import br.com.bipos.smartposapi.user.AppUserRepository
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class PosAuthService(
    private val repository: PosCredentialRepository,
    private val userRepository: AppUserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: PosJwtService,
    private val auditService: PosAuditService
) {

    fun login(
        request: PosAuthRequest,
        httpRequest: HttpServletRequest
    ): PosAuthResponse {

        val document = request.document.replace(Regex("[^0-9]"), "")

        val credential = repository.findByCnpjAndActiveTrue(document)
            ?: loginFailed(httpRequest)

        if (!passwordEncoder.matches(request.password, credential.passwordHash)) {
            loginFailed(httpRequest)
        }

        /* Atualiza dados do POS */
        credential.serialNumber = request.serialNumber
        credential.posVersion = request.posVersion
        repository.save(credential)

        auditService.log(
            companyId = credential.company.id,
            action = AuditAction.LOGIN_SUCCESS.name,
            request = httpRequest,
            serialNumber = credential.serialNumber,
            posVersion = credential.posVersion
        )

        val token = jwtService.generateToken(credential)

        val company = credential.company

        // 🔥 USER VISUAL (OWNER)
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
                name = owner?.name ?: company.name,          // fallback elegante
                photoPath = owner?.photoUrl
            ),

            pos = PosSnapshot(
                serialNumber = credential.serialNumber,
                version = credential.posVersion
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
