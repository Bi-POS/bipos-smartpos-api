package br.com.bipos.smartposapi.security

import org.springframework.security.core.context.SecurityContextHolder
import java.util.*

object PosSecurityUtils {

    fun principal(): PosPrincipal =
        SecurityContextHolder.getContext().authentication?.principal as? PosPrincipal
            ?: throw IllegalStateException("POS não autenticado")

    fun companyId(): UUID = principal().companyId
    fun userId(): UUID = principal().userId
    fun serialNumber(): String = principal().serialNumber
}
