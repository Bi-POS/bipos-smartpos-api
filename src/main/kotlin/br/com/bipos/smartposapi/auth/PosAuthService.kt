package br.com.bipos.smartposapi.auth

import br.com.bipos.smartposapi.audit.AuditAction
import br.com.bipos.smartposapi.audit.PosAuditService
import br.com.bipos.smartposapi.auth.dto.*
import br.com.bipos.smartposapi.credential.PosDevice
import br.com.bipos.smartposapi.credential.PosDeviceRepository
import br.com.bipos.smartposapi.domain.user.AppUser
import br.com.bipos.smartposapi.exception.InvalidPosCredentialsException
import br.com.bipos.smartposapi.exception.InvalidQrTokenException
import br.com.bipos.smartposapi.exception.QrTokenExpiredException
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
        return buildAuthResponse(user, pos)
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
        logger.info(
            "POS QR login attempt: serial={}, token={}",
            request.serialNumber,
            maskToken(request.qrToken)
        )

        val now = Instant.now()

        val qrToken = qrTokenRepository
            .findByTokenAndUsedFalse(request.qrToken)
            ?: run {
                logger.warn(
                    "POS QR login failed: token not found or already used. serial={}, token={}",
                    request.serialNumber,
                    maskToken(request.qrToken)
                )
                throw InvalidQrTokenException()
            }

        if (qrToken.expiresAt.isBefore(now)) {
            logger.warn(
                "POS QR login failed: token expired. serial={}, token={}, expiresAt={}",
                request.serialNumber,
                maskToken(request.qrToken),
                qrToken.expiresAt
            )
            throw QrTokenExpiredException()
        }

        qrToken.used = true
        qrTokenRepository.save(qrToken)

        val user = userRepository.findById(
            qrToken.userId
        ).orElseThrow {
            logger.warn(
                "POS QR login failed: user not found. serial={}, userId={}",
                request.serialNumber,
                qrToken.userId
            )
            InvalidPosCredentialsException()
        }

        val pos = posDeviceRepository
            .findBySerialNumberAndActiveTrue(request.serialNumber)
            ?: run {
                logger.warn(
                    "POS QR login failed: active POS not found. userId={}, serial={}",
                    user.id,
                    request.serialNumber
                )
                throw InvalidPosCredentialsException()
            }

        if (pos.company.id.toString() != qrToken.companyId.toString()) {
            logger.warn(
                "POS QR login failed: company mismatch. userId={}, tokenCompanyId={}, posCompanyId={}, serial={}",
                user.id,
                qrToken.companyId,
                pos.company.id,
                request.serialNumber
            )
            throw InvalidPosCredentialsException()
        }

        logger.info(
            "POS QR login validated. userId={}, companyId={}, serial={}",
            user.id,
            pos.company.id,
            pos.serialNumber
        )

        pos.lastSeenAt = LocalDateTime.now()
        pos.posVersion = request.posVersion
        posDeviceRepository.save(pos)

        auditService.log(
            companyId = pos.company.id,
            action = AuditAction.LOGIN_QR_SUCCESS.name,
            request = httpRequest,
            serialNumber = pos.serialNumber,
            posVersion = pos.posVersion
        )

        return buildAuthResponse(
            user = user,
            pos = pos
        )
    }

    private fun buildAuthResponse(
        user: AppUser,
        pos: PosDevice
    ): PosAuthResponse {
        val token = jwtService.generateAccessToken(
            user = user,
            pos = pos
        )
        val company = user.company
            ?: throw IllegalStateException("Usuário sem empresa")

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

    private fun maskToken(token: String?): String? {
        if (token.isNullOrBlank()) return null
        if (token.length <= 6) return "***"

        return "${token.take(3)}***${token.takeLast(3)}"
    }
}

