package br.com.bipos.smartposapi.auth

import br.com.bipos.smartposapi.credential.PosDevice
import br.com.bipos.smartposapi.domain.user.AppUser
import br.com.bipos.smartposapi.security.PosJwtProperties
import br.com.bipos.smartposapi.security.PosPrincipal
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
    companion object {
        private const val TOKEN_TYPE = "POS"
        private const val CLAIM_TYPE = "type"
        private const val CLAIM_COMPANY_ID = "companyId"
        private const val CLAIM_USER_ID = "userId"
        private const val CLAIM_SERIAL_NUMBER = "serialNumber"
        private const val CLAIM_POS_VERSION = "posVersion"
        private const val CLAIM_ROLE = "role"
    }

    private val key: Key by lazy {
        Keys.hmacShaKeyFor(
            Decoders.BASE64.decode(props.secret)
        )
    }

    fun generateAccessToken(
        user: AppUser,
        pos: PosDevice
    ): String {
        val userId = user.id
            ?: throw IllegalStateException("Usuário sem ID")
        val companyId = user.company?.id
            ?: throw IllegalStateException("Usuário sem empresa")
        val now = Date()
        val exp = Date(now.time + props.expiration)

        val claims = Jwts.claims().apply {
            subject = userId.toString()
            this[CLAIM_TYPE] = TOKEN_TYPE
            this[CLAIM_COMPANY_ID] = companyId.toString()
            this[CLAIM_USER_ID] = userId.toString()
            this[CLAIM_SERIAL_NUMBER] = pos.serialNumber
            this[CLAIM_POS_VERSION] = pos.posVersion
            this[CLAIM_ROLE] = user.role.name
        }

        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(exp)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }

    fun extractSubject(token: String): String =
        extractClaim(token) { it.subject }

    fun isTokenExpired(token: String): Boolean {
        val expiration = extractClaim(token) { it.expiration }
        return expiration.before(Date())
    }

    fun extractType(token: String): String =
        extractClaim(token) { it[CLAIM_TYPE] as String }

    fun extractPosPrincipal(token: String): PosPrincipal {
        val claims = extractAllClaims(token)
        return PosPrincipal(
            userId = UUID.fromString(claims[CLAIM_USER_ID] as String),
            companyId = UUID.fromString(claims[CLAIM_COMPANY_ID] as String),
            serialNumber = claims[CLAIM_SERIAL_NUMBER] as String
        )
    }

    fun extractAllClaims(token: String): Claims =
        Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body

    fun extractUserId(token: String): UUID =
        extractPosPrincipal(token).userId

    fun extractCompanyId(token: String): UUID =
        extractPosPrincipal(token).companyId

    fun extractSerialNumber(token: String): String =
        extractPosPrincipal(token).serialNumber

    fun extractPosVersion(token: String): String? =
        extractAllClaims(token)[CLAIM_POS_VERSION] as String?

    private fun <T> extractClaim(token: String, resolver: (Claims) -> T): T {
        val claims = extractAllClaims(token)
        return resolver(claims)
    }
}
