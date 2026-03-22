package br.com.bipos.smartposapi.security

import br.com.bipos.smartposapi.auth.PosJwtService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class PosJwtAuthenticationFilter(
    private val jwtService: PosJwtService,
    private val errorResponseWriter: SecurityErrorResponseWriter
) : OncePerRequestFilter() {
    companion object {
        private val appLogger = LoggerFactory.getLogger(PosJwtAuthenticationFilter::class.java)
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean =
        request.servletPath.startsWith("/pos/auth")

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")

        if (authHeader.isNullOrBlank() || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        try {
            val token = authHeader.substring(7)

            if (jwtService.isTokenExpired(token)) {
                SecurityContextHolder.clearContext()
                appLogger.warn(
                    "Rejected POS token: expired token for path={}",
                    request.requestURI
                )
                errorResponseWriter.write(
                    response = response,
                    status = HttpStatus.UNAUTHORIZED,
                    message = "Token POS inválido ou expirado",
                    path = request.requestURI
                )
                return
            }

            if (jwtService.extractType(token) != "POS") {
                SecurityContextHolder.clearContext()
                appLogger.warn(
                    "Rejected POS token: invalid token type for path={}",
                    request.requestURI
                )
                errorResponseWriter.write(
                    response = response,
                    status = HttpStatus.UNAUTHORIZED,
                    message = "Token POS inválido ou expirado",
                    path = request.requestURI
                )
                return
            }

            val principal = jwtService.extractPosPrincipal(token)

            val authentication = UsernamePasswordAuthenticationToken(
                principal,
                null,
                listOf(SimpleGrantedAuthority("ROLE_POS"))
            )

            SecurityContextHolder.getContext().authentication = authentication

        } catch (ex: Exception) {
            SecurityContextHolder.clearContext()
            appLogger.warn(
                "Rejected POS token: failed to parse token for path={}",
                request.requestURI,
                ex
            )
            errorResponseWriter.write(
                response = response,
                status = HttpStatus.UNAUTHORIZED,
                message = "Token POS inválido ou expirado",
                path = request.requestURI
            )
            return
        }

        filterChain.doFilter(request, response)
    }
}

