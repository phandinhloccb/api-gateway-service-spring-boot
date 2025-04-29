package com.loc.microservices.api_gateway_service_spring_boot.routes;

import java.net.URI;

import org.springframework.cloud.gateway.server.mvc.filter.CircuitBreakerFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class Routes {

    @Value("${product.service.url}")
    private String productServiceUrl;
    @Value("${order.service.url}")
    private String orderServiceUrl;
    @Value("${inventory.service.url}")
    private String inventoryServiceUrl;


    @Bean
    public RouterFunction<ServerResponse> productServiceRoute() {
        return GatewayRouterFunctions.route("product_service")
        .route(RequestPredicates.path("/api/product"), HandlerFunctions.http(productServiceUrl))
        // .filter(CircuitBreakerFilterFunctions.circuitBreaker("productServiceCircuitBreaker", 
        //     URI.create("forward:/fallbackRoute")))
        .build();
    }

    @Bean
    public RouterFunction<ServerResponse> orderServiceRoute() {
        return GatewayRouterFunctions.route("order_service")
        .route(RequestPredicates.path("/api/order"), HandlerFunctions.http(orderServiceUrl))
        // .filter(CircuitBreakerFilterFunctions.circuitBreaker("orderServiceCircuitBreaker", 
        //     URI.create("forward:/fallbackRoute")))
        .build();
    }

    @Bean
    public RouterFunction<ServerResponse> inventoryServiceRoute() {
        return GatewayRouterFunctions.route("inventory_service")
        .route(RequestPredicates.path("/api/inventory"), HandlerFunctions.http(inventoryServiceUrl))
        // .filter(CircuitBreakerFilterFunctions.circuitBreaker("inventoryServiceCircuitBreaker", 
        //     URI.create("forward:/fallbackRoute")))
        .build();
    }

    @Bean
    public RouterFunction<ServerResponse> fallbackRoute() {
        return GatewayRouterFunctions.route("fallbackRoute")
        .GET("/fallbackRoute", _->ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
        .body("Service is currently unavailable. Please try again later.dsdsf"))
        .build();
    }
}
