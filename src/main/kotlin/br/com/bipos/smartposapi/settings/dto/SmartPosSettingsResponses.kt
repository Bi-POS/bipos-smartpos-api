package br.com.bipos.smartposapi.settings.dto

import jakarta.validation.constraints.NotBlank

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

data class PinValidationResponse(
    val valid: Boolean,
    val message: String
)
