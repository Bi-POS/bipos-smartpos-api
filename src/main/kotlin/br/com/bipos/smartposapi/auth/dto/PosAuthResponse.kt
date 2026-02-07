package br.com.bipos.smartposapi.auth.dto

data class PosAuthResponse(
    val token: String,

    val company: CompanySnapshot,
    val user: UserSnapshot,
    val pos: PosSnapshot
)

data class CompanySnapshot(
    val id: String,
    val name: String,
    val cnpj: String?,
    val logoPath: String?,
    val email: String?,
    val phone: String?
)

data class UserSnapshot(
    val id: String?,
    val name: String,
    val photoPath: String?,
    val email: String?,
    val role: String?,
)

data class PosSnapshot(
    val serialNumber: String?,
    val version: String?
)
