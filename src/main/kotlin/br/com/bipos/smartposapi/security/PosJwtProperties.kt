package br.com.bipos.smartposapi.security

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "security.jwt.pos")
data class PosJwtProperties(
    val secret: String,
    val expiration: Long
)
