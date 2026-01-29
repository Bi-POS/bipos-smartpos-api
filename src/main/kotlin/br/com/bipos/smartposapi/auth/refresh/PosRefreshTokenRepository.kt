package br.com.bipos.smartposapi.auth.refresh

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface PosRefreshTokenRepository :
    JpaRepository<PosRefreshToken, UUID> {

    fun findByTokenAndActiveTrue(token: String): PosRefreshToken?
}
