package br.com.bipos.smartposapi.security

import br.com.bipos.smartposapi.auth.PosAuthContext
import br.com.bipos.smartposapi.domain.user.AppUser
import org.springframework.security.core.context.SecurityContextHolder
import java.util.*

object PosSecurityUtils {

    fun principal(): PosPrincipal =
        SecurityContextHolder.getContext().authentication?.principal as? PosPrincipal
            ?: throw IllegalStateException("POS não autenticado")

    fun authContext(user: AppUser): PosAuthContext =
        principal().toAuthContext(user)

    fun companyId(): UUID = principal().companyId
    fun userId(): UUID = principal().userId
    fun serialNumber(): String = principal().serialNumber
}
