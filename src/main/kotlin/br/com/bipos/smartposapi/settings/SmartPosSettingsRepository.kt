package br.com.bipos.smartposapi.settings

import br.com.bipos.smartposapi.domain.settings.SmartPosSettings
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface SmartPosSettingsRepository : JpaRepository<SmartPosSettings, UUID> {

    fun findByCompanyId(companyId: UUID): Optional<SmartPosSettings>

    @Modifying
    @Query("UPDATE SmartPosSettings s SET s.pinAttempts = s.pinAttempts + 1, s.updatedAt = CURRENT_TIMESTAMP WHERE s.companyId = :companyId")
    fun incrementPinAttempts(@Param("companyId") companyId: UUID)

    @Modifying
    @Query("UPDATE SmartPosSettings s SET s.pinAttempts = 0, s.updatedAt = CURRENT_TIMESTAMP WHERE s.companyId = :companyId")
    fun resetPinAttempts(@Param("companyId") companyId: UUID)
}