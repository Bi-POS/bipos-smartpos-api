package br.com.bipos.smartposapi.auth

import br.com.bipos.smartposapi.audit.AuditAction
import br.com.bipos.smartposapi.audit.PosAuditService
import br.com.bipos.smartposapi.auth.dto.*
import br.com.bipos.smartposapi.credential.PosDeviceRepository
import br.com.bipos.smartposapi.exception.InvalidPosCredentialsException
import br.com.bipos.smartposapi.exception.InvalidQrTokenException
import br.com.bipos.smartposapi.login.PosQrLoginRequest
import br.com.bipos.smartposapi.login.SmartPosQrTokenRepository
import br.com.bipos.smartposapi.user.AppUserRepository
import jakarta.servlet.http.HttpServletRequest
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDateTime

@Service
class PosAuthService(
    private val posDeviceRepository: PosDeviceRepository,
    private val userRepository: AppUserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: PosJwtService,
    private val auditService: PosAuditService,
    private val qrTokenRepository: SmartPosQrTokenRepository
) {
    companion object {
        private val logger = LoggerFactory.getLogger(PosAuthService::class.java)
    }

    fun login(
        request: PosAuthRequest,
        httpRequest: HttpServletRequest
    ): PosAuthResponse {
        val normalizedEmail = request.email
            ?.trim()
            ?.lowercase()
            ?.takeIf { it.isNotBlank() }
        val normalizedDocument = request.document
            ?.replace(Regex("[^0-9]"), "")
            ?.takeIf { it.isNotBlank() }

        logger.info(
            "POS login attempt: email={}, document={}, serial={}, version={}",
            maskEmail(normalizedEmail),
            maskDocument(normalizedDocument),
            request.serialNumber,
            request.posVersion
        )

        val user = when {
            normalizedEmail != null -> {
                userRepository.findByEmailAndActiveTrue(
                    normalizedEmail
                )
            }

            normalizedDocument != null -> {
                userRepository.findByDocumentAndActiveTrue(normalizedDocument)
            }

            else -> {
                logger.warn(
                    "POS login failed: missing email/document. serial={}",
                    request.serialNumber
                )
                loginFailed(httpRequest)
            }
        } ?: run {
            logger.warn(
                "POS login failed: active user not found. email={}, document={}, serial={}",
                maskEmail(normalizedEmail),
                maskDocument(normalizedDocument),
                request.serialNumber
            )
            loginFailed(httpRequest)
        }

        val passwordOk = passwordEncoder.matches(
            request.password,
            user.passwordHash
        )

        if (!passwordOk) {
            logger.warn(
                "POS login failed: password mismatch. userId={}, companyId={}, serial={}",
                user.id,
                user.company?.id,
                request.serialNumber
            )
            loginFailed(httpRequest)
        }

        val pos = posDeviceRepository
            .findBySerialNumberAndActiveTrue(request.serialNumber)
            ?: run {
                logger.warn(
                    "POS login failed: active POS not found. userId={}, companyId={}, serial={}",
                    user.id,
                    user.company?.id,
                    request.serialNumber
                )
                loginFailed(httpRequest)
            }

        if (pos.company.id != user.company?.id) {
            logger.warn(
                "POS login failed: company mismatch. userId={}, userCompanyId={}, posCompanyId={}, serial={}",
                user.id,
                user.company?.id,
                pos.company.id,
                request.serialNumber
            )
            loginFailed(httpRequest)
        }

        logger.info(
            "POS login validated. userId={}, companyId={}, serial={}",
            user.id,
            pos.company.id,
            pos.serialNumber
        )

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

        val company = user.company
            ?: throw IllegalStateException("Usuário sem empresa")

        /* 9️⃣ Retorno */
        return PosAuthResponse(
            token = token,

            company = CompanySnapshot(
                id = company.id.toString(),
                name = company.name,
                cnpj = company.document,
                logoPath = company.logoUrl,
                email = company.email,
                phone = company.phone
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

    private fun loginFailed(httpRequest: HttpServletRequest): Nothing {
        auditService.log(
            companyId = null,
            action = AuditAction.LOGIN_FAILED.name,
            request = httpRequest
        )
        throw InvalidPosCredentialsException()
    }

    private fun maskEmail(email: String?): String? {
        if (email.isNullOrBlank()) return null

        val parts = email.split("@", limit = 2)
        if (parts.size != 2) return "***"

        val name = parts[0]
        val maskedName = when {
            name.length <= 2 -> "${name.first()}***"
            else -> "${name.take(2)}***"
        }

        return "$maskedName@${parts[1]}"
    }

    private fun maskDocument(document: String?): String? {
        if (document.isNullOrBlank()) return null
        if (document.length <= 4) return "***"

        return "${"*".repeat(document.length - 4)}${document.takeLast(4)}"
    }

    @Transactional
    fun loginWithQr(
        request: PosQrLoginRequest,
        httpRequest: HttpServletRequest
    ): PosAuthResponse {

        println("========== LOGIN POS QR ==========")
        println("SERIAL: ${request.serialNumber}")
        println("QR TOKEN: ${request.qrToken}")

        val now = Instant.now()

        // 1️⃣ Busca token no banco
        val qrToken = qrTokenRepository
            .findByTokenAndUsedFalse(request.qrToken)
            ?: throw InvalidQrTokenException()

        // 2️⃣ Verifica expiração
        if (qrToken.expiresAt.isBefore(now)) {
            throw InvalidQrTokenException()
        }

        // 3️⃣ Marca token como usado
        qrToken.used = true
        qrTokenRepository.save(qrToken)

        // 4️⃣ Carrega usuário
        val user = userRepository.findById(
            qrToken.userId
        ).orElseThrow {
            InvalidPosCredentialsException()
        }

        // 5️⃣ Carrega POS
        val pos = posDeviceRepository
            .findBySerialNumberAndActiveTrue(request.serialNumber)
            ?: throw InvalidPosCredentialsException()

        // 6️⃣ Garante empresa correta
        if (pos.company.id.toString() != qrToken.companyId.toString()) {
            throw InvalidPosCredentialsException()
        }

        println("✅ LOGIN QR VALIDATED")

        // 7️⃣ Atualiza POS
        pos.lastSeenAt = LocalDateTime.now()
        pos.posVersion = request.posVersion
        posDeviceRepository.save(pos)

        // 8️⃣ Auditoria
        auditService.log(
            companyId = pos.company.id,
            action = AuditAction.LOGIN_QR_SUCCESS.name,
            request = httpRequest,
            serialNumber = pos.serialNumber,
            posVersion = pos.posVersion
        )

        // 9️⃣ Gera JWT POS
        val token = jwtService.generateToken(
            user = user,
            pos = pos
        )

        val company = user.company
            ?: throw IllegalStateException("Usuário sem empresa")

        // 🔟 Retorno padrão
        return PosAuthResponse(
            token = token,

            company = CompanySnapshot(
                id = company.id.toString(),
                name = company.name,
                cnpj = company.document,
                logoPath = company.logoUrl,
                email = company.email,
                phone = company.phone
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

