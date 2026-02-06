package br.com.bipos.smartposapi.auth.refresh

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.util.*

interface PosRefreshTokenRepository : JpaRepository<PosRefreshToken, UUID> {

    fun findByTokenAndActiveTrue(token: String): PosRefreshToken?

    @Modifying
    @Query("update PosRefreshToken r set r.active = false where r.serialNumber = :serial")
    fun deactivateAllBySerialNumber(serial: String)

    @Modifying
    @Query("update PosRefreshToken r set r.active = false where r.userId = :userId")
    fun deactivateAllByUserId(userId: UUID)
}
