package br.com.bipos.smartposapi.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: PosJwtAuthenticationFilter,
    private val errorResponseWriter: SecurityErrorResponseWriter
) {

    @Bean
    fun posFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }

            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }

            .exceptionHandling {
                it.authenticationEntryPoint { request, response, _ ->
                    errorResponseWriter.write(
                        response = response,
                        status = HttpStatus.UNAUTHORIZED,
                        message = "Autenticação POS obrigatória",
                        path = request.requestURI
                    )
                }

                it.accessDeniedHandler { request, response, _ ->
                    errorResponseWriter.write(
                        response = response,
                        status = HttpStatus.FORBIDDEN,
                        message = "Acesso negado",
                        path = request.requestURI
                    )
                }
            }

            .authorizeHttpRequests {
                it.requestMatchers(
                    "/pos/auth/login",
                    "/pos/auth/refresh",
                    "/pos/auth/login/qr"
                ).permitAll()

                it.requestMatchers("/pos/**").hasRole("POS")

                it.anyRequest().denyAll()
            }

            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter::class.java
            )

        return http.build()
    }
}




