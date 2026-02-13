// domain/settings/SmartPosPrint.kt
package br.com.bipos.smartposapi.domain.settings

enum class SmartPosPrint {
    NONE, SHORT, FULL;

    companion object {
        fun fromString(value: String): SmartPosPrint {
            return try {
                valueOf(value.uppercase())
            } catch (e: IllegalArgumentException) {
                FULL
            }
        }
    }
}