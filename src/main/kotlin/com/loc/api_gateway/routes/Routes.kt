package com.loc.api_gateway.routes

import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.gateway.server.mvc.filter.CircuitBreakerFilterFunctions
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.web.servlet.function.RequestPredicates
import org.springframework.web.servlet.function.RouterFunction
import org.springframework.web.servlet.function.ServerResponse
import java.net.URI

@Configuration
class Routes {

    @Value("\${services.product.url}")
    private lateinit var productServiceUrl: String

    @Value("\${services.order.url}")
    private lateinit var orderServiceUrl: String

    @Value("\${services.inventory.url}")
    private lateinit var inventoryServiceUrl: String

    @Bean
    fun productServiceRoute(): RouterFunction<ServerResponse> {
        return GatewayRouterFunctions.route("product_service")
            .route(RequestPredicates.path("/api/product/**"), HandlerFunctions.http(productServiceUrl))
            .filter(CircuitBreakerFilterFunctions.circuitBreaker("productServiceCircuitBreaker", 
                URI.create("forward:/fallbackRoute")))
            .build()
    }

    @Bean
    fun orderServiceRoute(): RouterFunction<ServerResponse> {
        return GatewayRouterFunctions.route("order_service")
            .route(RequestPredicates.path("/api/order/**"), HandlerFunctions.http(orderServiceUrl))
            .filter(CircuitBreakerFilterFunctions.circuitBreaker("orderServiceCircuitBreaker", 
                URI.create("forward:/fallbackRoute")))
            .build()
    }

    @Bean
    fun inventoryServiceRoute(): RouterFunction<ServerResponse> {
        return GatewayRouterFunctions.route("inventory_service")
            .route(RequestPredicates.path("/api/inventory/**"), HandlerFunctions.http(inventoryServiceUrl))
            .filter(CircuitBreakerFilterFunctions.circuitBreaker("inventoryServiceCircuitBreaker", 
                URI.create("forward:/fallbackRoute")))
            .build()
    }

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