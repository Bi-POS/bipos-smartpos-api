package br.com.bipos.smartposapi.security

import br.com.bipos.smartposapi.auth.PosAuthContext
import br.com.bipos.smartposapi.auth.PosJwtService
import br.com.bipos.smartposapi.user.AppUserRepository
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
                response.sendError(HttpServletResponse.SC_FORBIDDEN)
                return
            }

            if (jwtService.extractType(token) != "POS") {
                response.sendError(HttpServletResponse.SC_FORBIDDEN)
                return
            }

            val principal = PosPrincipal(
                userId = jwtService.extractUserId(token),
                companyId = jwtService.extractCompanyId(token),
                serialNumber = jwtService.extractSerialNumber(token)
            )

            val authentication = UsernamePasswordAuthenticationToken(
                principal,
                null,
                listOf(SimpleGrantedAuthority("ROLE_POS"))
            )

            SecurityContextHolder.getContext().authentication = authentication

        } catch (ex: Exception) {
            SecurityContextHolder.clearContext()
            response.sendError(HttpServletResponse.SC_FORBIDDEN)
            return
        }

        filterChain.doFilter(request, response)
    }
}

