package br.com.bipos.smartposapi.settings.dto

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.LocalDateTime
import java.util.UUID

data class SmartPosSettingsResponse(
    val id: UUID,
    val saleOperationMode: String,
    val print: String,
    val printType: String,
    val printLogo: Boolean,
    val logoConfigured: Boolean,
    val logoUrl: String?,
    val securityEnabled: Boolean,
    val hasPin: Boolean,
    val lastPinChange: LocalDateTime?,
    val pinAttempts: Int,
    val autoLogoutMinutes: Int,
    val darkMode: Boolean,
    val soundEnabled: Boolean,
    val availableModules: List<SmartPosAvailableModuleResponse>,
    val version: Long,
    val updatedAt: LocalDateTime
)

data class SmartPosAvailableModuleResponse(
    val id: UUID,
    val companyId: UUID,
    val moduleId: UUID,
    val moduleName: String,
    val moduleType: String,
    val enabled: Boolean,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?,
    val activatedAt: LocalDateTime?,
    val deactivatedAt: LocalDateTime?
)

data class PrintTypeResponse(
    val printType: String
)

data class PrintLogoResponse(
    val printLogo: Boolean
)

data class LogoUrlResponse(
    val logoUrl: String?
)

data class AutoLogoutResponse(
    val autoLogoutMinutes: Int
)

data class DarkModeResponse(
    val darkMode: Boolean
)

data class SoundEnabledResponse(
    val soundEnabled: Boolean
)

data class PinValidationRequest(
    @field:NotBlank(message = "PIN é obrigatório")
    val pin: String
)

data class SmartPosSecurityRequest(
    @JsonProperty("enabled")
    val enabled: Boolean = false,

    @field:Size(min = 4, max = 6, message = "PIN deve ter entre 4 e 6 dígitos")
    @field:Pattern(regexp = "\\d+", message = "PIN deve conter apenas números")
    @JsonProperty("pin")
    val pin: String? = null
)

data class UpdateSmartPosSettingsRequest(
    val saleOperationMode: String? = null,
    val print: String? = null,
    val printType: String? = null,
    val printLogo: Boolean? = null,
    val logoUrl: String? = null,
    val security: SmartPosSecurityRequest? = null,
    val securityEnabled: Boolean? = null,
    @field:Min(1, message = "Auto logout deve ser no mínimo 1 minuto")
    @field:Max(60, message = "Auto logout deve ser no máximo 60 minutos")
    val autoLogoutMinutes: Int? = null,
    val darkMode: Boolean? = null,
    val soundEnabled: Boolean? = null
)

data class UpdatePinRequest(
    @field:NotBlank(message = "PIN é obrigatório")
    val pin: String
)

data class PinValidationResponse(
    val valid: Boolean,
    val message: String
)

data class PinConfigurationResponse(
    val securityEnabled: Boolean,
    val hasPin: Boolean,
    val message: String
)
