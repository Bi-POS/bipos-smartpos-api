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

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return request.servletPath.startsWith("/pos/auth")
    }

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

        val token = authHeader.substring(7)

        try {
            val companyId = jwtService.extractCompanyId(token)
            val type = jwtService.extractType(token)


            if (type != "POS" || jwtService.isTokenExpired(token)) {
                SecurityContextHolder.clearContext()
                filterChain.doFilter(request, response)
                return
            }

            val principal = PosPrincipal(
                companyId = companyId,
                tokenType = type,
            )

            val authentication = UsernamePasswordAuthenticationToken(
                principal,
                null,
                listOf(SimpleGrantedAuthority("ROLE_POS"))
            )

            SecurityContextHolder.getContext().authentication = authentication

        } catch (ex: Exception) {
            SecurityContextHolder.clearContext()
        }

        filterChain.doFilter(request, response)
    }
}

