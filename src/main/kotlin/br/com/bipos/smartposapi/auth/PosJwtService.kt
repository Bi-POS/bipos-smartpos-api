package br.com.bipos.smartposapi.auth

import br.com.bipos.smartposapi.credential.PosCredential
import br.com.bipos.smartposapi.security.PosJwtProperties
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import java.security.Key
import java.util.*

@Service
class PosJwtService(
    private val props: PosJwtProperties
) {
    private val POS_TOKEN_VALIDITY_MS = 1000L * 60 * 60 * 2


    private val key: Key by lazy {
        Keys.hmacShaKeyFor(
            Decoders.BASE64.decode(props.secret)
        )
    }

    private val validityMs: Long
        get() = props.expiration

    // ===============================
    // TOKEN GENERATION
    // ===============================

    fun generateToken(credential: PosCredential): String {
        val now = Date()
        val exp = Date(now.time + POS_TOKEN_VALIDITY_MS)

        val claims = Jwts.claims().apply {
            subject = credential.cnpj
            this["companyId"] = credential.company.id.toString()
            this["serialNumber"] = credential.serialNumber
            this["posVersion"] = credential.posVersion
            this["type"] = "POS"
        }

        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(exp)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }

    // ===============================
    // VALIDATION
    // ===============================

    fun extractCnpj(token: String): String =
        extractClaim(token) { it.subject }

    fun isTokenExpired(token: String): Boolean {
        val expiration = extractClaim(token) { it.expiration }
        return expiration.before(Date())
    }

    // ===============================
    // CLAIMS
    // ===============================

    fun extractCompanyId(token: String): UUID =
        UUID.fromString(extractAllClaims(token)["companyId"] as String)

    fun extractType(token: String): String =
        extractAllClaims(token)["type"] as String

    fun extractAllClaims(token: String): Claims =
        Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body

    // ===============================
    // PRIVATE
    // ===============================

    private fun <T> extractClaim(token: String, resolver: (Claims) -> T): T {
        val claims = extractAllClaims(token)
        return resolver(claims)
    }
}
