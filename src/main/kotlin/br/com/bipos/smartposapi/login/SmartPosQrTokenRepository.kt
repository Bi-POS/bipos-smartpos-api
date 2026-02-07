package br.com.bipos.smartposapi.login

import br.com.bipos.smartposapi.domain.auth.SmartPosQrToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.Instant

interface SmartPosQrTokenRepository :
    JpaRepository<SmartPosQrToken, String> {

    /**
     * Busca token válido (não usado)
     */
    fun findByTokenAndUsedFalse(token: String): SmartPosQrToken?

    /**
     * Remove tokens expirados (opcional – limpeza)
     */
    @Modifying
    @Query(
        """
        delete from SmartPosQrToken t
        where t.expiresAt < :now
        """
    )
    fun deleteExpired(now: Instant)
}
