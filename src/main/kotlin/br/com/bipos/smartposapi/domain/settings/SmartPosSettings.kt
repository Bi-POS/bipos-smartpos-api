// domain/settings/SmartPosSettings.kt
package br.com.bipos.smartposapi.domain.settings

import jakarta.persistence.*
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "smartpos_settings")
class SmartPosSettings(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(nullable = false, unique = true)
    val companyId: UUID,

    // ===== IMPRESSÃO =====
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var print: SmartPosPrint = SmartPosPrint.FULL,

    @Column(nullable = false)
    var printLogo: Boolean = false,

    @Column(nullable = true, length = 500)
    var logoUrl: String? = null,

    // ===== SEGURANÇA =====
    @Column(nullable = false)
    var securityEnabled: Boolean = false,

    @Column(nullable = true)
    var pinHash: String? = null,

    @Column(nullable = false)
    var pinAttempts: Int = 0,

    @Column(nullable = true)
    var lastPinChange: LocalDateTime? = null,

    // ===== COMPORTAMENTO =====
    @Column(nullable = false)
    var autoLogoutMinutes: Int = 5,

    @Column(nullable = false)
    var darkMode: Boolean = false,

    @Column(nullable = false)
    var soundEnabled: Boolean = true,

    // ===== CONTROLE =====
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var version: Long = 1,

    @Column(nullable = false)
    var isActive: Boolean = true
) {

    fun validatePin(pin: String, passwordEncoder: PasswordEncoder): Boolean {
        if (!securityEnabled || pinHash == null) return true

        val isValid = passwordEncoder.matches(pin, pinHash)

        if (isValid) {
            pinAttempts = 0
        } else {
            pinAttempts++
        }

        updatedAt = LocalDateTime.now()
        return isValid
    }

    fun isPinBlocked(): Boolean = pinAttempts >= 5

    fun registerPinAttempt(success: Boolean) {
        if (success) {
            pinAttempts = 0
        } else {
            pinAttempts++
        }
        updatedAt = LocalDateTime.now()
    }
}