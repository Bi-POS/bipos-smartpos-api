package br.com.bipos.smartposapi.auth.refresh

import br.com.bipos.smartposapi.exception.InvalidRefreshTokenException
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

@Service
class PosRefreshTokenService(
    private val repository: PosRefreshTokenRepository
) {

    /* 🔄 Cria refresh token no LOGIN */
    fun create(
        userId: UUID,
        companyId: UUID,
        serialNumber: String
    ): PosRefreshToken {

        val refreshToken = PosRefreshToken(
            token = UUID.randomUUID().toString(),
            userId = userId,
            companyId = companyId,
            serialNumber = serialNumber,
            expiresAt = Instant.now().plus(30, ChronoUnit.DAYS)
        )

        return repository.save(refreshToken)
    }

    /* 🔍 Valida refresh token */
    fun validate(token: String): PosRefreshToken {
        val refresh = repository.findByTokenAndActiveTrue(token)
            ?: throw InvalidRefreshTokenException()

        if (refresh.expiresAt.isBefore(Instant.now())) {
            refresh.active = false
            repository.save(refresh)
            throw InvalidRefreshTokenException()
        }

        return refresh
    }

    /* 🔒 Revoga refresh token (logout) */
    fun revoke(token: String) {
        val refresh = repository.findByTokenAndActiveTrue(token) ?: return

        refresh.active = false
        repository.save(refresh)
    }

    /* 🔒 Revoga TODAS as sessões de um POS */
    fun revokeAllForPos(serialNumber: String) {
        repository.deactivateAllBySerialNumber(serialNumber)
    }

    /* 🔒 Revoga TODAS as sessões de um usuário */
    fun revokeAllForUser(userId: UUID) {
        repository.deactivateAllByUserId(userId)
    }
}
