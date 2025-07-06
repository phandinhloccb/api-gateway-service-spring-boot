package com.loc.api_gateway.controller

import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import com.fasterxml.jackson.databind.ObjectMapper
import java.util.Base64

@RestController
@RequestMapping("/api/gateway")
class TestController {

    @Value("\${spring.security.oauth2.resourceserver.jwt.secret}")
    private lateinit var jwtSecret: String

    @GetMapping("/test")
    fun testJwtValidation(authentication: JwtAuthenticationToken): Map<String, Any> {
        val jwt = authentication.token
        
        return mapOf(
            "status" to "✅ JWT validation successful!",
            "user_id" to (jwt.getClaimAsString("userId") ?: jwt.getClaimAsString("sub") ?: "N/A"),
            "username" to (jwt.getClaimAsString("sub") ?: "N/A"),
            "email" to (jwt.getClaimAsString("email") ?: "N/A"),
            "role" to (jwt.getClaimAsString("role") ?: "N/A"),
            "expires_at" to (jwt.expiresAt?.toString() ?: "N/A"),
            "issued_at" to (jwt.issuedAt?.toString() ?: "N/A"),
            "algorithm" to (jwt.headers["alg"] ?: "N/A"),
            "all_claims" to jwt.claims
        )
    }

    @GetMapping("/test-simple")
    fun testSimple(@AuthenticationPrincipal jwt: Jwt): Map<String, Any> {
        return mapOf(
            "user" to (jwt.getClaimAsString("sub") ?: "unknown"),
            "role" to (jwt.getClaimAsString("role") ?: "N/A"),
            "email" to (jwt.getClaimAsString("email") ?: "N/A"),
            "status" to "authenticated"
        )
    }

    @GetMapping("/test-admin")
    fun testAdminRole(@AuthenticationPrincipal jwt: Jwt): Map<String, Any> {
        val role = jwt.getClaimAsString("role")

        return if (role == "ADMIN") {
            mapOf(
                "message" to "✅ Admin access granted",
                "user" to jwt.getClaimAsString("sub"),
                "role" to role,
                "email" to jwt.getClaimAsString("email")
            )
        } else {
            mapOf(
                "error" to "❌ Admin role required",
                "current_role" to (role ?: "N/A"),
                "user" to jwt.getClaimAsString("sub")
            )
        }
    }

    @GetMapping("/test-no-auth")
    fun testNoAuth(): Map<String, String> {
        return mapOf(
            "message" to "✅ This endpoint works without authentication",
            "timestamp" to java.time.LocalDateTime.now().toString()
        )
    }

    @GetMapping("/debug-jwt")
    fun debugJwt(@RequestHeader("Authorization") authHeader: String): Map<String, Any> {
        println("🔍 DEBUG: debugJwt function called!")
        println("🔍 DEBUG: authHeader = $authHeader")
        
        return try {
            val token = authHeader.replace("Bearer ", "")
            println("🔍 DEBUG: token after removing Bearer = $token")
            
            val parts = token.split(".")
            println("🔍 DEBUG: token parts count = ${parts.size}")
            
            if (parts.size != 3) {
                println("🔍 DEBUG: Invalid JWT format - returning error")
                return mapOf("error" to "Invalid JWT format", "parts_count" to parts.size)
            }
            
            println("🔍 DEBUG: JWT format is valid, processing...")
            
            val header = String(Base64.getUrlDecoder().decode(parts[0]))
            val payload = String(Base64.getUrlDecoder().decode(parts[1]))
            val signature = parts[2]
            
            println("🔍 DEBUG: header = $header")
            println("🔍 DEBUG: payload = $payload")
            println("🔍 DEBUG: signature = $signature")
            
            // Manual signature verification
            val headerAndPayload = "${parts[0]}.${parts[1]}"
            val expectedSignature = generateHmacSha384Signature(headerAndPayload, jwtSecret)
            
            println("🔍 DEBUG: expected signature = $expectedSignature")
            println("🔍 DEBUG: signatures match = ${signature == expectedSignature}")
            
            mapOf(
                "status" to "✅ Debug function executed successfully",
                "gateway_secret" to jwtSecret,
                "gateway_secret_length" to jwtSecret.length,
                "gateway_secret_hash" to jwtSecret.hashCode(),
                "jwt_header" to header,
                "jwt_payload" to payload,
                "jwt_signature" to signature,
                "expected_signature" to expectedSignature,
                "signatures_match" to (signature == expectedSignature),
                "token_parts_count" to parts.size
            )
        } catch (e: Exception) {
            println("🔍 DEBUG: Exception occurred = ${e.message}")
            e.printStackTrace()
            mapOf("error" to "Failed to debug JWT: ${e.message}")
        }
    }
    
    private fun generateHmacSha384Signature(data: String, secret: String): String {
        val hmac = javax.crypto.Mac.getInstance("HmacSHA384")
        val secretKey = javax.crypto.spec.SecretKeySpec(secret.toByteArray(), "HmacSHA384")
        hmac.init(secretKey)
        val signature = hmac.doFinal(data.toByteArray())
        return Base64.getUrlEncoder().withoutPadding().encodeToString(signature)
    }
} 