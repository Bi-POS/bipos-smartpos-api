package br.com.bipos.smartposapi.settings

import br.com.bipos.smartposapi.security.PosSecurityUtils
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/pos/settings")
class SmartPosSettingsController(
    private val settingsService: SmartPosSettingsService
) {

    @GetMapping("/print-type")
    fun getPrintType(): ResponseEntity<Map<String, String>> {
        val companyId = PosSecurityUtils.companyId()
        val printType = settingsService.getPrintType(companyId)
        return ResponseEntity.ok(mapOf("printType" to printType))
    }

    @GetMapping("/print-logo")
    fun shouldPrintLogo(): ResponseEntity<Map<String, Boolean>> {
        val companyId = PosSecurityUtils.companyId()
        val shouldPrint = settingsService.shouldPrintLogo(companyId)
        return ResponseEntity.ok(mapOf("printLogo" to shouldPrint))
    }

    @GetMapping("/logo-url")
    fun getLogoUrl(): ResponseEntity<Map<String, String?>> {
        val companyId = PosSecurityUtils.companyId()
        val logoUrl = settingsService.getLogoUrl(companyId)
        return ResponseEntity.ok(mapOf("logoUrl" to logoUrl))
    }

    @GetMapping("/auto-logout")
    fun getAutoLogout(): ResponseEntity<Map<String, Int>> {
        val companyId = PosSecurityUtils.companyId()
        val minutes = settingsService.getAutoLogoutMinutes(companyId)
        return ResponseEntity.ok(mapOf("autoLogoutMinutes" to minutes))
    }

    @GetMapping("/dark-mode")
    fun isDarkMode(): ResponseEntity<Map<String, Boolean>> {
        val companyId = PosSecurityUtils.companyId()
        val darkMode = settingsService.isDarkMode(companyId)
        return ResponseEntity.ok(mapOf("darkMode" to darkMode))
    }

    @GetMapping("/sound-enabled")
    fun isSoundEnabled(): ResponseEntity<Map<String, Boolean>> {
        val companyId = PosSecurityUtils.companyId()
        val soundEnabled = settingsService.isSoundEnabled(companyId)
        return ResponseEntity.ok(mapOf("soundEnabled" to soundEnabled))
    }

    @PostMapping("/validate-pin")
    fun validatePin(
        @RequestBody request: PinValidationRequest
    ): ResponseEntity<PinValidationResponse> {
        val companyId = PosSecurityUtils.companyId()
        val isValid = settingsService.validatePin(companyId, request.pin)

        return ResponseEntity.ok(
            PinValidationResponse(
                valid = isValid,
                message = if (isValid) "PIN válido" else "PIN inválido"
            )
        )
    }
}

data class PinValidationRequest(
    val pin: String
)

data class PinValidationResponse(
    val valid: Boolean,
    val message: String
)
