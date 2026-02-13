package br.com.bipos.smartposapi.settings

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/pos/settings")
class SmartPosSettingsController(
    private val settingsService: SmartPosSettingsService
) {

    @GetMapping("/{companyId}/print-type")
    fun getPrintType(
        @PathVariable companyId: UUID
    ): ResponseEntity<Map<String, String>> {
        val printType = settingsService.getPrintType(companyId)
        return ResponseEntity.ok(mapOf("printType" to printType))
    }

    @GetMapping("/{companyId}/print-logo")
    fun shouldPrintLogo(
        @PathVariable companyId: UUID
    ): ResponseEntity<Map<String, Boolean>> {
        val shouldPrint = settingsService.shouldPrintLogo(companyId)
        return ResponseEntity.ok(mapOf("printLogo" to shouldPrint))
    }

    @GetMapping("/{companyId}/logo-url")
    fun getLogoUrl(
        @PathVariable companyId: UUID
    ): ResponseEntity<Map<String, String?>> {
        val logoUrl = settingsService.getLogoUrl(companyId)
        return ResponseEntity.ok(mapOf("logoUrl" to logoUrl))
    }

    @GetMapping("/{companyId}/auto-logout")
    fun getAutoLogout(
        @PathVariable companyId: UUID
    ): ResponseEntity<Map<String, Int>> {
        val minutes = settingsService.getAutoLogoutMinutes(companyId)
        return ResponseEntity.ok(mapOf("autoLogoutMinutes" to minutes))
    }

    @GetMapping("/{companyId}/dark-mode")
    fun isDarkMode(
        @PathVariable companyId: UUID
    ): ResponseEntity<Map<String, Boolean>> {
        val darkMode = settingsService.isDarkMode(companyId)
        return ResponseEntity.ok(mapOf("darkMode" to darkMode))
    }

    @GetMapping("/{companyId}/sound-enabled")
    fun isSoundEnabled(
        @PathVariable companyId: UUID
    ): ResponseEntity<Map<String, Boolean>> {
        val soundEnabled = settingsService.isSoundEnabled(companyId)
        return ResponseEntity.ok(mapOf("soundEnabled" to soundEnabled))
    }

    @PostMapping("/{companyId}/validate-pin")
    fun validatePin(
        @PathVariable companyId: UUID,
        @RequestBody request: PinValidationRequest
    ): ResponseEntity<PinValidationResponse> {
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