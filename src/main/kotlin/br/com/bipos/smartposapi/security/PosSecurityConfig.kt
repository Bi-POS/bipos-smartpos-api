package br.com.bipos.smartposapi.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: PosJwtAuthenticationFilter
) {

    // 🔹 POS
    @Bean
    @Order(1)
    fun posFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .securityMatcher("/pos/**")
            .csrf { it.disable() }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authorizeHttpRequests {
                it.requestMatchers("/pos/auth/**").permitAll()
                it.anyRequest().authenticated()
            }
            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter::class.java
            )

        return http.build()
    }

    // 🔹 Fallback (OBRIGATÓRIA)
    @Bean
    @Order(2)
    fun fallbackFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .securityMatcher("/**")
            .csrf { it.disable() }
            .authorizeHttpRequests {
                it.anyRequest().denyAll()
            }

        return http.build()
    }
}



