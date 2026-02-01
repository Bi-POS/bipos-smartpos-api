package br.com.bipos.smartposapi.security

import br.com.bipos.smartposapi.auth.PosJwtService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class PosJwtAuthenticationFilter(
    private val jwtService: PosJwtService
) : OncePerRequestFilter() {

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(PosJwtAuthenticationFilter::class.java)
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val skip = request.servletPath.startsWith("/pos/auth")
        if (skip) {
            log.debug("🔓 POS | Skip auth route: {}", request.servletPath)
        }
        return skip
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val path = request.servletPath
        val method = request.method

        log.info("➡️ POS AUTH | {} {}", method, path)

        val authHeader = request.getHeader("Authorization")

        if (authHeader.isNullOrBlank()) {
            log.warn("⛔ POS AUTH | Authorization header ausente")
            filterChain.doFilter(request, response)
            return
        }

        if (!authHeader.startsWith("Bearer ")) {
            log.warn("⛔ POS AUTH | Authorization não é Bearer")
            filterChain.doFilter(request, response)
            return
        }

        val token = authHeader.substring(7)

        try {
            if (jwtService.isTokenExpired(token)) {
                log.warn("⛔ POS AUTH | Token expirado")
                throw RuntimeException("Token expirado")
            }

            val type = jwtService.extractType(token)
            log.info("🔍 POS AUTH | Token type = {}", type)

            if (type != "POS") {
                log.warn("⛔ POS AUTH | Token não é POS (type={})", type)
                throw RuntimeException("Token inválido")
            }

            val companyId = jwtService.extractCompanyId(token)
            log.info("🏢 POS AUTH | companyId = {}", companyId)

            val principal = PosPrincipal(
                companyId = companyId,
                tokenType = type
            )

            val authentication = UsernamePasswordAuthenticationToken(
                principal,
                null,
                listOf(SimpleGrantedAuthority("ROLE_POS"))
            )

            SecurityContextHolder.getContext().authentication = authentication

            log.info("✅ POS AUTH | Authentication setado com sucesso")

        } catch (ex: Exception) {
            log.error("❌ POS AUTH | Falha na autenticação: {}", ex.message)
            SecurityContextHolder.clearContext()
            response.sendError(HttpServletResponse.SC_FORBIDDEN)
            return
        }

        filterChain.doFilter(request, response)
    }
}

