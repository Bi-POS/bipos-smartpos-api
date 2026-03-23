package br.com.bipos.smartposapi.settings

import br.com.bipos.smartposapi.company.CompanyRepository
import br.com.bipos.smartposapi.domain.company.Company
import br.com.bipos.smartposapi.domain.companymodule.CompanyModule
import br.com.bipos.smartposapi.domain.settings.SmartPosPrint
import br.com.bipos.smartposapi.domain.settings.SmartPosSaleOperationMode
import br.com.bipos.smartposapi.domain.settings.SmartPosSettings
import br.com.bipos.smartposapi.exception.BusinessException
import br.com.bipos.smartposapi.exception.ResourceNotFoundException
import br.com.bipos.smartposapi.settings.dto.SmartPosAvailableModuleResponse
import br.com.bipos.smartposapi.settings.dto.SmartPosSecurityRequest
import br.com.bipos.smartposapi.settings.dto.SmartPosSettingsResponse
import br.com.bipos.smartposapi.settings.dto.UpdateSmartPosSettingsRequest
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class SmartPosSettingsService(
    private val repository: SmartPosSettingsRepository,
    private val companyRepository: CompanyRepository,
    private val passwordEncoder: BCryptPasswordEncoder
) {

    @Transactional
    fun getSettings(companyId: UUID): SmartPosSettingsResponse {
        val company = loadCompany(companyId)
        val settings = getOrCreateActiveSettings(companyId)
        return settings.toResponse(company)
    }

    @Transactional
    fun validatePin(companyId: UUID, pin: String): Boolean {
        val settings = getOrCreateActiveSettings(companyId)

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
    ): SmartPosSettingsResponse {
        val company = loadCompany(companyId)
        val settings = getOrCreateActiveSettings(companyId)

        request.saleOperationMode?.let { settings.saleOperationMode = SmartPosSaleOperationMode.fromString(it) }
        (request.print ?: request.printType)?.let { settings.print = SmartPosPrint.fromString(it) }
        request.printLogo?.let { settings.printLogo = it }
        request.logoUrl?.let { settings.logoUrl = it.ifBlank { null } }
        synchronizeLogoState(settings)
        applySecurity(settings, request)
        request.autoLogoutMinutes?.let {
            validateAutoLogoutMinutes(it)
            settings.autoLogoutMinutes = it
        }
        request.darkMode?.let { settings.darkMode = it }
        request.soundEnabled?.let { settings.soundEnabled = it }

        touch(settings)
        val saved = repository.save(settings)
        return saved.toResponse(company)
    }

    @Transactional
    fun updatePin(companyId: UUID, pin: String): SmartPosSettings {
        val settings = getOrCreateActiveSettings(companyId)
        settings.updatePin(passwordEncoder.encode(pin))
        settings.securityEnabled = true

        touch(settings)
        return repository.save(settings)
    }

    @Transactional
    fun getPrintType(companyId: UUID): String =
        getOrCreateActiveSettings(companyId).print.name

    @Transactional
    fun shouldPrintLogo(companyId: UUID): Boolean =
        getOrCreateActiveSettings(companyId).printLogo

    @Transactional
    fun getLogoUrl(companyId: UUID): String? =
        getOrCreateActiveSettings(companyId).logoUrl

    @Transactional
    fun getAutoLogoutMinutes(companyId: UUID): Int =
        getOrCreateActiveSettings(companyId).autoLogoutMinutes

    @Transactional
    fun isDarkMode(companyId: UUID): Boolean =
        getOrCreateActiveSettings(companyId).darkMode

    @Transactional
    fun isSoundEnabled(companyId: UUID): Boolean =
        getOrCreateActiveSettings(companyId).soundEnabled

    fun resolveOperationMode(companyId: UUID): SmartPosSaleOperationMode {
        return repository.findByCompanyIdAndIsActiveTrue(companyId)
            .map { it.saleOperationMode }
            .orElse(SmartPosSaleOperationMode.DIRECT)
    }

    private fun getOrCreateActiveSettings(companyId: UUID): SmartPosSettings {
        return repository.findByCompanyIdAndIsActiveTrue(companyId)
            .orElseGet { repository.save(SmartPosSettings(companyId = companyId)) }
    }

    private fun loadCompany(companyId: UUID): Company =
        companyRepository.findById(companyId)
            .orElseThrow { ResourceNotFoundException("Empresa não encontrada") }

    private fun applySecurity(
        settings: SmartPosSettings,
        request: UpdateSmartPosSettingsRequest
    ) {
        request.security?.let {
            applyContractSecurity(settings, it)
            return
        }

        request.securityEnabled?.let { enabled ->
            settings.securityEnabled = enabled
            if (!enabled) {
                settings.pinHash = null
                settings.resetPinAttempts()
                settings.lastPinChange = null
            }
        }
    }

    private fun applyContractSecurity(
        settings: SmartPosSettings,
        security: SmartPosSecurityRequest
    ) {
        when {
            !security.enabled -> {
                settings.securityEnabled = false
                settings.pinHash = null
                settings.resetPinAttempts()
                settings.lastPinChange = null
            }
            security.pin.isNullOrBlank() -> {
                throw BusinessException("PIN é obrigatório quando segurança está ativada")
            }
            !security.pin.matches(Regex("\\d{4,6}")) -> {
                throw BusinessException("PIN deve ter entre 4 e 6 dígitos numéricos")
            }
            else -> {
                settings.securityEnabled = true
                settings.updatePin(passwordEncoder.encode(security.pin))
            }
        }
    }

    private fun validateAutoLogoutMinutes(value: Int) {
        if (value !in 1..60) {
            throw BusinessException("Tempo de logout deve estar entre 1 e 60 minutos")
        }
    }

    private fun synchronizeLogoState(settings: SmartPosSettings) {
        if (!settings.printLogo) {
            settings.logoUrl = null
            return
        }

        if (settings.logoUrl.isNullOrBlank()) {
            throw BusinessException("URL da logo é obrigatória quando printLogo é true")
        }
    }

    private fun touch(settings: SmartPosSettings) {
        settings.updatedAt = LocalDateTime.now()
        settings.version += 1
    }

    private fun SmartPosSettings.toResponse(company: Company): SmartPosSettingsResponse {
        val settingsId = requireNotNull(id) { "Settings ID não pode ser nulo" }

        return SmartPosSettingsResponse(
            id = settingsId,
            saleOperationMode = saleOperationMode.name,
            print = print.name,
            printType = print.name,
            printLogo = printLogo,
            logoConfigured = !logoUrl.isNullOrBlank(),
            logoUrl = logoUrl,
            securityEnabled = securityEnabled,
            hasPin = !pinHash.isNullOrBlank(),
            lastPinChange = lastPinChange,
            pinAttempts = pinAttempts,
            autoLogoutMinutes = autoLogoutMinutes,
            darkMode = darkMode,
            soundEnabled = soundEnabled,
            availableModules = company.modules.mapNotNull { it.toResponse(company.id ?: companyId) },
            version = version,
            updatedAt = updatedAt
        )
    }

    private fun CompanyModule.toResponse(companyId: UUID): SmartPosAvailableModuleResponse? {
        val companyModuleId = id ?: return null
        val moduleEntity = module ?: return null
        val moduleId = moduleEntity.id ?: return null

        return SmartPosAvailableModuleResponse(
            id = companyModuleId,
            companyId = companyId,
            moduleId = moduleId,
            moduleName = moduleEntity.name.name,
            moduleType = moduleEntity.name.name,
            enabled = true,
            createdAt = null,
            updatedAt = null,
            activatedAt = null,
            deactivatedAt = null
        )
    }
}
