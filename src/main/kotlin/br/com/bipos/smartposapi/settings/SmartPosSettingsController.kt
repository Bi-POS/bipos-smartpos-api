package br.com.bipos.smartposapi.settings

import br.com.bipos.smartposapi.security.PosSecurityUtils
import br.com.bipos.smartposapi.settings.dto.AutoLogoutResponse
import br.com.bipos.smartposapi.settings.dto.DarkModeResponse
import br.com.bipos.smartposapi.settings.dto.LogoUrlResponse
import br.com.bipos.smartposapi.settings.dto.PinValidationRequest
import br.com.bipos.smartposapi.settings.dto.PinConfigurationResponse
import br.com.bipos.smartposapi.settings.dto.PinValidationResponse
import br.com.bipos.smartposapi.settings.dto.PrintLogoResponse
import br.com.bipos.smartposapi.settings.dto.PrintTypeResponse
import br.com.bipos.smartposapi.settings.dto.SmartPosSettingsResponse
import br.com.bipos.smartposapi.settings.dto.SoundEnabledResponse
import br.com.bipos.smartposapi.settings.dto.UpdatePinRequest
import br.com.bipos.smartposapi.settings.dto.UpdateSmartPosSettingsRequest
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/pos/settings")
class SmartPosSettingsController(
    private val settingsService: SmartPosSettingsService
) {
    @GetMapping
    fun getSettings(): SmartPosSettingsResponse =
        settingsService.getSettings(currentCompanyId())

    @GetMapping("/print-type")
    fun getPrintType(): PrintTypeResponse =
        PrintTypeResponse(
            printType = settingsService.getPrintType(currentCompanyId())
        )

    @GetMapping("/print-logo")
    fun shouldPrintLogo(): PrintLogoResponse =
        PrintLogoResponse(
            printLogo = settingsService.shouldPrintLogo(currentCompanyId())
        )

    @GetMapping("/logo-url")
    fun getLogoUrl(): LogoUrlResponse =
        LogoUrlResponse(
            logoUrl = settingsService.getLogoUrl(currentCompanyId())
        )

    @GetMapping("/auto-logout")
    fun getAutoLogout(): AutoLogoutResponse =
        AutoLogoutResponse(
            autoLogoutMinutes = settingsService.getAutoLogoutMinutes(currentCompanyId())
        )

    @GetMapping("/dark-mode")
    fun isDarkMode(): DarkModeResponse =
        DarkModeResponse(
            darkMode = settingsService.isDarkMode(currentCompanyId())
        )

    @GetMapping("/sound-enabled")
    fun isSoundEnabled(): SoundEnabledResponse =
        SoundEnabledResponse(
            soundEnabled = settingsService.isSoundEnabled(currentCompanyId())
        )

    @PostMapping("/validate-pin")
    fun validatePin(
        @RequestBody @Valid request: PinValidationRequest
    ): PinValidationResponse {
        val isValid = settingsService.validatePin(currentCompanyId(), request.pin)
        return PinValidationResponse(
            valid = isValid,
            message = if (isValid) "PIN válido" else "PIN inválido"
        )
    }

    @PatchMapping
    fun updateSettings(
        @RequestBody @Valid request: UpdateSmartPosSettingsRequest
    ): SmartPosSettingsResponse =
        settingsService.updateSettings(currentCompanyId(), request)

    @PutMapping("/pin")
    fun updatePin(
        @RequestBody @Valid request: UpdatePinRequest
    ): PinConfigurationResponse {
        val settings = settingsService.updatePin(currentCompanyId(), request.pin)
        return PinConfigurationResponse(
            securityEnabled = settings.securityEnabled,
            hasPin = !settings.pinHash.isNullOrBlank(),
            message = "PIN atualizado com sucesso"
        )
    }

    private fun currentCompanyId(): UUID = PosSecurityUtils.companyId()
}
