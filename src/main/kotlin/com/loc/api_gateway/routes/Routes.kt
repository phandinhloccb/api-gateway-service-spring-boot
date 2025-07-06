package com.loc.api_gateway.routes

import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.gateway.server.mvc.filter.CircuitBreakerFilterFunctions
import org.springframework.cloud.gateway.server.mvc.filter.FilterFunctions
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.servlet.function.RequestPredicates
import org.springframework.web.servlet.function.RouterFunction
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.web.servlet.function.ServerResponse
import java.net.URI
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import mu.KotlinLogging

@Configuration
class Routes {

    private val log = KotlinLogging.logger {}

    @Value("\${services.auth.url}")
    private lateinit var authServiceUrl: String

    @Value("\${services.product.url}")
    private lateinit var productServiceUrl: String

    @Value("\${services.order.url}")
    private lateinit var orderServiceUrl: String

    @Value("\${services.inventory.url}")
    private lateinit var inventoryServiceUrl: String

    private val objectMapper = jacksonObjectMapper()

    private fun addUserHeaders(request: ServerRequest): ServerRequest {

        val authHeader = request.headers().firstHeader("Authorization")

        return if (authHeader != null && authHeader.startsWith("Bearer ")) {
            val token = authHeader.substring(7)

            try {
                val jwt = parseJwtToken(token)
                val userId = jwt["userId"] as? String ?: ""
                val email = jwt["email"] as? String ?: ""
                val role = jwt["role"] as? String ?: ""
                val username = jwt["sub"] as? String ?: ""
                
                ServerRequest.from(request)
                    .header("X-User-Id", userId)
                    .header("X-User-Email", email)
                    .header("X-User-Role", role)
                    .header("X-User-Username", username)
                    .build()
            } catch (e: Exception) {
                log.info{"Error parsing JWT: ${e.message}"}
                request
            }
        } else {
            log.info{"No Authorization header found"}
            request
        }
    }

    // Helper function để parse JWT token
    private fun parseJwtToken(token: String): Map<String, Any> {
        val parts = token.split(".")
        if (parts.size != 3) {
            throw IllegalArgumentException("Invalid JWT token format")
        }
        
        val payload = parts[1]
        val decodedPayload = java.util.Base64.getUrlDecoder().decode(payload)
        val payloadJson = String(decodedPayload)
        
        println("🔍 JWT Payload: $payloadJson")
        
        // Parse JSON using ObjectMapper
        return objectMapper.readValue<Map<String, Any>>(payloadJson)
    }

    // ========== API ROUTES ==========

    @Bean
    fun authServiceRoute(): RouterFunction<ServerResponse> {
        return GatewayRouterFunctions.route("auth_service")
            .route(RequestPredicates.path("/api/auth/**"), HandlerFunctions.http(authServiceUrl))
            .filter(CircuitBreakerFilterFunctions.circuitBreaker("authServiceCircuitBreaker",
                URI.create("forward:/fallbackRoute")))
            .build()
    }

    @Bean
    fun productServiceRoute(): RouterFunction<ServerResponse> {
        return GatewayRouterFunctions.route("product_service")
            .route(RequestPredicates.path("/api/product/**"), HandlerFunctions.http(productServiceUrl))
            .filter(CircuitBreakerFilterFunctions.circuitBreaker("productServiceCircuitBreaker", 
                URI.create("forward:/fallbackRoute")))
            .before { request ->
                println("🔍 ProductService: Processing request")
                addUserHeaders(request)
            }
            .build()
    }

    @Bean
    fun orderServiceRoute(): RouterFunction<ServerResponse> {
        return GatewayRouterFunctions.route("order_service")
            .route(RequestPredicates.path("/api/order/**"), HandlerFunctions.http(orderServiceUrl))
            .filter(CircuitBreakerFilterFunctions.circuitBreaker("orderServiceCircuitBreaker", 
                URI.create("forward:/fallbackRoute")))
            .before { request ->
                println("🔍 OrderService: Processing request")
                addUserHeaders(request)
            }
            .build()
    }

    @Bean
    fun inventoryServiceRoute(): RouterFunction<ServerResponse> {
        return GatewayRouterFunctions.route("inventory_service")
            .route(RequestPredicates.path("/api/inventory/**"), HandlerFunctions.http(inventoryServiceUrl))
            .filter(CircuitBreakerFilterFunctions.circuitBreaker("inventoryServiceCircuitBreaker", 
                URI.create("forward:/fallbackRoute")))
            .before { request ->
                println("🔍 InventoryService: Processing request")
                addUserHeaders(request)
            }
            .build()
    }

    // ========== SWAGGER DOCUMENTATION ROUTES ==========

    @Bean
    fun productServiceSwaggerRoute(): RouterFunction<ServerResponse> {
        return GatewayRouterFunctions.route("product_service_swagger")
            .route(RequestPredicates.path("/aggregate/product-service/v3/api-docs"), 
                   HandlerFunctions.http(productServiceUrl))
            .filter(CircuitBreakerFilterFunctions.circuitBreaker("productServiceSwaggerCircuitBreaker",
                    URI.create("forward:/fallbackRoute")))
            .filter(FilterFunctions.setPath("/v3/api-docs"))
            .build()
    }

    @Bean
    fun orderServiceSwaggerRoute(): RouterFunction<ServerResponse> {
        return GatewayRouterFunctions.route("order_service_swagger")
            .route(RequestPredicates.path("/aggregate/order-service/v3/api-docs"),
                   HandlerFunctions.http(orderServiceUrl))
            .filter(CircuitBreakerFilterFunctions.circuitBreaker("orderServiceSwaggerCircuitBreaker",
                    URI.create("forward:/fallbackRoute")))
            .filter(FilterFunctions.setPath("/v3/api-docs"))
            .build()
    }

    @Bean
    fun inventoryServiceSwaggerRoute(): RouterFunction<ServerResponse> {
        return GatewayRouterFunctions.route("inventory_service_swagger")
            .route(RequestPredicates.path("/aggregate/inventory-service/v3/api-docs"),
                   HandlerFunctions.http(inventoryServiceUrl))
            .filter(CircuitBreakerFilterFunctions.circuitBreaker("inventoryServiceSwaggerCircuitBreaker",
                    URI.create("forward:/fallbackRoute")))
            .filter(FilterFunctions.setPath("/v3/api-docs"))
            .build()
    }

    // ========== FALLBACK ROUTE ==========

    @Bean
    fun fallbackRoute(): RouterFunction<ServerResponse> {
        return GatewayRouterFunctions.route("fallbackRoute")
            .GET("/fallbackRoute") { 
                ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Service is currently unavailable. Please try again later.")
            }
            .build()
    }
}