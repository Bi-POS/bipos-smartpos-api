package br.com.bipos.smartposapi.auth

import br.com.bipos.smartposapi.domain.user.AppUser
import java.util.UUID

data class PosAuthContext(
    val user: AppUser,
    val companyId: UUID,
    val serialNumber: String
)