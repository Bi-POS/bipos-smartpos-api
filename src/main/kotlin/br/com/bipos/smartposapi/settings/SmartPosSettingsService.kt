// service/SmartPosSettingsService.kt
package br.com.bipos.smartposapi.settings

import br.com.bipos.smartposapi.domain.settings.SmartPosPrint
import br.com.bipos.smartposapi.domain.settings.SmartPosSettings
import br.com.bipos.smartposapi.exception.BusinessException
import br.com.bipos.smartposapi.exception.ResourceNotFoundException
import br.com.bipos.smartposapi.settings.dto.UpdateSmartPosSettingsRequest
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class SmartPosSettingsService(
    private val repository: SmartPosSettingsRepository,
    private val passwordEncoder: BCryptPasswordEncoder
) {

    @Transactional(readOnly = true)
    fun getSettings(companyId: UUID): SmartPosSettings {
        return repository.findByCompanyId(companyId)
            .orElseThrow { ResourceNotFoundException("Configurações não encontradas para a empresa") }
    }

    @Transactional
    fun validatePin(companyId: UUID, pin: String): Boolean {
        val settings = repository.findByCompanyId(companyId)
            .orElseThrow { ResourceNotFoundException("Configurações não encontradas") }

        if (!settings.securityEnabled || settings.pinHash == null) {
            return true
        }

        if (settings.isPinBlocked()) {
            throw BusinessException("PIN bloqueado temporariamente por muitas tentativas")
        }

        val isValid = passwordEncoder.matches(pin, settings.pinHash)
        settings.registerPinAttempt(isValid)
        repository.save(settings)

        return isValid
    }

    @Transactional
    fun updateSettings(
        companyId: UUID,
        request: UpdateSmartPosSettingsRequest
    ): SmartPosSettings {
        val settings = getOrCreateSettings(companyId)

        request.printType?.let { settings.print = parsePrintType(it) }
        request.printLogo?.let { settings.printLogo = it }
        request.logoUrl?.let { settings.logoUrl = it.ifBlank { null } }
        request.securityEnabled?.let { settings.securityEnabled = it }
        request.autoLogoutMinutes?.let { settings.autoLogoutMinutes = it }
        request.darkMode?.let { settings.darkMode = it }
        request.soundEnabled?.let { settings.soundEnabled = it }

        touch(settings)
        return repository.save(settings)
    }

    @Transactional
    fun updatePin(companyId: UUID, pin: String): SmartPosSettings {
        val settings = getOrCreateSettings(companyId)
        settings.pinHash = passwordEncoder.encode(pin)
        settings.securityEnabled = true
        settings.pinAttempts = 0
        settings.lastPinChange = LocalDateTime.now()

        touch(settings)
        return repository.save(settings)
    }

    @Transactional
    fun getPrintType(companyId: UUID): String {
        val settings = repository.findByCompanyId(companyId)
            .orElseThrow { ResourceNotFoundException("Configurações não encontradas") }
        return settings.print.name
    }

    @Transactional(readOnly = true)
    fun shouldPrintLogo(companyId: UUID): Boolean {
        val settings = repository.findByCompanyId(companyId)
            .orElseThrow { ResourceNotFoundException("Configurações não encontradas") }
        return settings.printLogo
    }

    @Transactional(readOnly = true)
    fun getLogoUrl(companyId: UUID): String? {
        val settings = repository.findByCompanyId(companyId)
            .orElseThrow { ResourceNotFoundException("Configurações não encontradas") }
        return settings.logoUrl
    }

    @Transactional(readOnly = true)
    fun getAutoLogoutMinutes(companyId: UUID): Int {
        val settings = repository.findByCompanyId(companyId)
            .orElseThrow { ResourceNotFoundException("Configurações não encontradas") }
        return settings.autoLogoutMinutes
    }

    @Transactional(readOnly = true)
    fun isDarkMode(companyId: UUID): Boolean {
        val settings = repository.findByCompanyId(companyId)
            .orElseThrow { ResourceNotFoundException("Configurações não encontradas") }
        return settings.darkMode
    }

    @Transactional(readOnly = true)
    fun isSoundEnabled(companyId: UUID): Boolean {
        val settings = repository.findByCompanyId(companyId)
            .orElseThrow { ResourceNotFoundException("Configurações não encontradas") }
        return settings.soundEnabled
    }

    private fun getOrCreateSettings(companyId: UUID): SmartPosSettings {
        return repository.findByCompanyId(companyId)
            .orElseGet { SmartPosSettings(companyId = companyId) }
    }

    private fun parsePrintType(value: String): SmartPosPrint {
        return try {
            SmartPosPrint.valueOf(value.uppercase())
        } catch (_: IllegalArgumentException) {
            throw BusinessException("Tipo de impressão inválido")
        }
    }

    private fun touch(settings: SmartPosSettings) {
        settings.updatedAt = LocalDateTime.now()
        settings.version += 1
    }
}
