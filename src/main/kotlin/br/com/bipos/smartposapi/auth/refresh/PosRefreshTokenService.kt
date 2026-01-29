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

    fun create(companyId: UUID): PosRefreshToken {
        val refreshToken = PosRefreshToken(
            companyId = companyId,
            token = UUID.randomUUID().toString(),
            expiresAt = Instant.now().plus(30, ChronoUnit.DAYS)
        )

        return repository.save(refreshToken)
    }

    fun validate(token: String): PosRefreshToken {
        val refresh = repository.findByTokenAndActiveTrue(token)
            ?: throw InvalidRefreshTokenException()

        if (refresh.expiresAt.isBefore(Instant.now())) {
            throw InvalidRefreshTokenException()
        }

        return refresh
    }

    fun revoke(token: String) {
        val refresh = repository.findByTokenAndActiveTrue(token) ?: return

        refresh.active = false
        repository.save(refresh)
    }
}
