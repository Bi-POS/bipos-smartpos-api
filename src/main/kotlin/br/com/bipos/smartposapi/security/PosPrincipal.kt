package br.com.bipos.smartposapi.security

import br.com.bipos.smartposapi.auth.PosAuthContext
import br.com.bipos.smartposapi.domain.user.AppUser
import java.util.UUID

data class PosPrincipal(
    val userId: UUID,
    val companyId: UUID,
    val serialNumber: String
) {
    fun toAuthContext(user: AppUser): PosAuthContext =
        PosAuthContext(
            user = user,
            companyId = companyId,
            serialNumber = serialNumber
        )
}
