package br.com.bipos.smartposapi.auth

import br.com.bipos.smartposapi.audit.AuditAction
import br.com.bipos.smartposapi.audit.PosAuditService
import br.com.bipos.smartposapi.auth.dto.PosAuthRequest
import br.com.bipos.smartposapi.auth.dto.PosAuthResponse
import br.com.bipos.smartposapi.credential.PosCredentialRepository
import br.com.bipos.smartposapi.exception.InvalidPosCredentialsException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class PosAuthService(
    private val repository: PosCredentialRepository,
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

        // 🔄 Atualiza dados do POS
        credential.serialNumber = request.serialNumber
        credential.posVersion = request.posVersion
        repository.save(credential)

        // 🔍 Auditoria sucesso
        auditService.log(
            companyId = credential.company.id,
            action = AuditAction.LOGIN_SUCCESS.name,
            request = httpRequest,
            serialNumber = credential.serialNumber,
            posVersion = credential.posVersion
        )

        val token = jwtService.generateToken(credential)



        return PosAuthResponse(
            token = token,
            cnpj = credential.cnpj,
            companyId = credential.company.id.toString(),
            companyName = credential.company.name,
            serialNumber = credential.serialNumber,
            posVersion = credential.posVersion,
            companyLogoUrl = credential.company.logoUrl
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

