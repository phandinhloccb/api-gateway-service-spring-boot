package com.loc.api_gateway.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import javax.crypto.spec.SecretKeySpec

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Value("\${spring.security.oauth2.resourceserver.jwt.secret}")
    private lateinit var jwtSecret: String

    private val freeResourceUrls = arrayOf(
        "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**",
        "/swagger-resources/**", "/api-docs/**", "/aggregate/**",
        "/actuator/**", "/actuator/prometheus",
        "/api/auth/login", "/api/auth/refresh", "/api/gateway/test-no-auth",
        "/api/gateway/debug-jwt"
    )

    @Bean
    fun jwtDecoder(): JwtDecoder {
        val keyBytes = java.util.Base64.getDecoder().decode(jwtSecret)
        val secretKey = SecretKeySpec(keyBytes, "HmacSHA384")
        
        return NimbusJwtDecoder.withSecretKey(secretKey)
            .macAlgorithm(MacAlgorithm.HS384)
            .build()
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .authorizeHttpRequests { authorize ->
                authorize
                    .requestMatchers(*freeResourceUrls).permitAll()
                    .anyRequest().authenticated()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { }
            }
            .build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            allowedOrigins = listOf("http://localhost:4200")
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD")
            allowedHeaders = listOf("Authorization", "Content-Type", "X-Requested-With")
            allowCredentials = true
            exposedHeaders = listOf("Authorization")
        }
        
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
} 