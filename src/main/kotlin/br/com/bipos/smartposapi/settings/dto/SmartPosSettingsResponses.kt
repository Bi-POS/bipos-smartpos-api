package br.com.bipos.smartposapi.settings.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

data class SmartPosSettingsResponse(
    val printType: String,
    val printLogo: Boolean,
    val logoConfigured: Boolean,
    val logoUrl: String?,
    val securityEnabled: Boolean,
    val hasPin: Boolean,
    val autoLogoutMinutes: Int,
    val darkMode: Boolean,
    val soundEnabled: Boolean
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

data class UpdateSmartPosSettingsRequest(
    val printType: String? = null,
    val printLogo: Boolean? = null,
    val logoUrl: String? = null,
    val securityEnabled: Boolean? = null,
    @field:Min(1, message = "Auto logout deve ser no mínimo 1 minuto")
    @field:Max(240, message = "Auto logout deve ser no máximo 240 minutos")
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
