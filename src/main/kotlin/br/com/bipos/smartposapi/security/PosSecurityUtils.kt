package br.com.bipos.smartposapi.security

import org.springframework.security.core.context.SecurityContextHolder
import java.util.*

object PosSecurityUtils {

    fun getCompanyId(): UUID {
        val authentication = SecurityContextHolder
            .getContext()
            .authentication
            ?: throw IllegalStateException("POS não autenticado")

        val principal = authentication.principal
            ?: throw IllegalStateException("Principal não encontrado no contexto")

        return try {
            UUID.fromString(principal as String)
        } catch (ex: Exception) {
            throw IllegalStateException("Principal inválido para POS", ex)
        }
    }

    fun isAuthenticated(): Boolean =
        SecurityContextHolder.getContext().authentication != null
}
