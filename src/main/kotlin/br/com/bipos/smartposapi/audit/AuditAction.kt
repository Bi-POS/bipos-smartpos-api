package br.com.bipos.smartposapi.audit

enum class AuditAction {
    LOGIN_SUCCESS,
    LOGIN_QR_SUCCESS,
    LOGIN_FAILED,
    TOKEN_REFRESH,
    LOGOUT
}