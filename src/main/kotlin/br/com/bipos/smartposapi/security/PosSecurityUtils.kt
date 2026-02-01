package br.com.bipos.smartposapi.security

import org.springframework.security.core.context.SecurityContextHolder
import java.util.*

object PosSecurityUtils {

    fun getCompanyId(): UUID {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw IllegalStateException("No authentication in context")

        return when (val principal = authentication.principal) {
            is PosPrincipal -> principal.companyId
            is String -> UUID.fromString(principal) // fallback legado
            else -> throw IllegalStateException(
                "Unsupported principal type: ${principal::class.java.name}"
            )
        }
    }

    fun isAuthenticated(): Boolean =
        SecurityContextHolder.getContext().authentication != null
}
