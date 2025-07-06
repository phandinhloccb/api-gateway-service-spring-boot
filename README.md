# API Gateway Service - Clean Architecture Implementation

A Spring Boot microservice gateway built following Clean Architecture principles, implementing the Hexagonal Architecture pattern with JWT authentication, circuit breaker protection, and proper dependency inversion for microservices routing.

## 📋 Table of Contents

- [Architecture Overview](#architecture-overview)
- [Project Structure](#project-structure)
- [Clean Architecture Layers](#clean-architecture-layers)
- [Key Features](#key-features)
- [Technologies Used](#technologies-used)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Security Implementation](#security-implementation)
- [Circuit Breaker Pattern](#circuit-breaker-pattern)
- [User Context Propagation](#user-context-propagation)
- [Service Integration](#service-integration)
- [Testing Strategy](#testing-strategy)

## 🏗️ Architecture Overview

This project implements Clean Architecture with Hexagonal Architecture pattern for API Gateway, following these core principles:

- **Dependency Inversion**: Inner layers define interfaces (ports), outer layers implement them (adapters)
- **Separation of Concerns**: Each layer has a single responsibility
- **Framework Independence**: Business logic is isolated from frameworks
- **JWT Authentication**: Centralized authentication with user context propagation
- **Circuit Breaker Pattern**: Resilience and fault tolerance for microservices
- **Testability**: Easy to unit test with proper mocking

### Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    Controllers                              │
│              (HTTP/REST API Interface)                      │
│         TestController & Route Handlers                     │
└─────────────────────┬───────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────┐
│                  Application                                │
│          (Use Cases & Business Rules)                       │
│    JWT Processing │ User Header Injection                   │
└─────────────────────┬───────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────┐
│                    Domain                                   │
│              (Entities & Business Logic)                    │
│           JWT Claims │ User Context                         │
└─────────────────────┬───────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────┐
│                Infrastructure                               │
│         (Security, Routing, External Services)              │
│    SecurityConfig │ Routes │ Circuit Breakers               │
└─────────────────────────────────────────────────────────────┘
```

## 📁 Project Structure

```
src/main/kotlin/com/loc/api_gateway/
├── domain/                          # Enterprise Business Rules
│   └── model/
│       └── UserContext.kt          # Domain Entity (User Context)
│
├── application/                     # Application Business Rules
│   ├── service/
│   │   ├── JwtProcessingService.kt  # JWT Token Processing Use Case
│   │   └── UserHeaderService.kt     # User Header Injection Use Case
│   └── port/
│       └── AuthenticationPort.kt    # Authentication Contract (Port)
│
├── infrastructure/                  # Frameworks & Drivers
│   ├── adapter/
│   │   └── JwtAuthenticationAdapter.kt # JWT Authentication Implementation
│   ├── security/
│   │   └── SecurityConfig.kt        # Security Configuration
│   └── routing/
│       └── Routes.kt                # Gateway Routes Configuration
│
├── controller/                      # Interface Adapters
│   └── TestController.kt           # Test & Debug Controllers
│
└── configuration/
    └── BeanConfiguration.kt        # Dependency Injection Configuration
```

## 🎯 Clean Architecture Layers

### 1. Domain Layer (Innermost)
- **Purpose**: Contains enterprise business rules and entities
- **Dependencies**: None (pure business logic)
- **Components**:
  - `UserContext`: Core domain entity representing authenticated user
  - JWT Claims processing logic
  - User authentication state management

### 2. Application Layer
- **Purpose**: Contains application-specific business rules (use cases)
- **Dependencies**: Only depends on Domain layer
- **Components**:
  - `JwtProcessingService`: Handles JWT token parsing and validation
  - `UserHeaderService`: Manages user context header injection
  - `AuthenticationPort`: Port (interface) defining authentication contract

### 3. Infrastructure Layer (Outermost)
- **Purpose**: Contains frameworks, databases, external services
- **Dependencies**: Implements interfaces from inner layers
- **Components**:
  - `SecurityConfig`: Spring Security configuration with JWT
  - `Routes`: Gateway routing configuration with circuit breakers
  - `JwtAuthenticationAdapter`: Implements domain authentication contract

### 4. Controller Layer (Interface Adapters)
- **Purpose**: Handles HTTP requests and responses
- **Dependencies**: Uses Application layer services
- **Components**:
  - `TestController`: Debug and testing endpoints
  - Request/Response mapping and validation

## ✨ Key Features

- **Clean Architecture Implementation**: Proper dependency inversion and layer separation
- **JWT Authentication**: Centralized authentication with HS384 algorithm
- **Circuit Breaker Pattern**: Resilience4j for fault tolerance
- **User Context Propagation**: Automatic user header injection to downstream services
- **API Documentation Aggregation**: Centralized Swagger documentation
- **Comprehensive Testing**: Unit and integration tests for all layers
- **Security Configuration**: OAuth2 Resource Server with JWT validation

## 🛠️ Technologies Used

- **Framework**: Spring Boot 3.5.3
- **Language**: Kotlin 1.9.25
- **Gateway**: Spring Cloud Gateway Server WebMVC
- **Security**: Spring Security OAuth2 Resource Server
- **Circuit Breaker**: Resilience4j
- **Documentation**: SpringDoc OpenAPI 3.0
- **Testing**: JUnit 5, Spring Boot Test
- **Build Tool**: Gradle with Kotlin DSL
- **Runtime**: Java 17

## 🚀 Getting Started

### Prerequisites

- JDK 17 or higher
- Docker and Docker Compose (for Keycloak)
- Gradle 8.x or higher

### Running the Application

1. **Start External Services**:
   ```bash
   cd server
   docker-compose up -d
   ```

2. **Run the API Gateway**:
   ```bash
   ./gradlew bootRun
   ```

3. **Access the Services**:
   - API Gateway: http://localhost:9000
   - Swagger UI: http://localhost:9000/swagger-ui.html
   - Health Check: http://localhost:9000/actuator/health

### Configuration

The application uses `application.yaml` for configuration:

```yaml
spring:
  application:
    name: api-gateway
  security:
    oauth2:
      resourceserver:
        jwt:
          secret: MA9TFJ2cV/PxU1h68mbsq/vZB0LskxuAtsQFyZdrmBCNuGk+1pVS6YFIrQEEmnVV

server:
  port: 9000

# Microservice URLs
services:
  product:
    url: http://localhost:8080
  order:
    url: http://localhost:8081
  inventory:
    url: http://localhost:8082
  auth:
    url: http://localhost:8084
```

## 📚 API Documentation

### Gateway Endpoints

| Method | Endpoint | Description | Authentication |
|--------|----------|-------------|----------------|
| GET | `/api/gateway/test` | Test JWT validation | Required |
| GET | `/api/gateway/test-simple` | Simple authentication test | Required |
| GET | `/api/gateway/test-admin` | Admin role validation | Required (Admin) |
| GET | `/api/gateway/test-no-auth` | Public endpoint | None |
| GET | `/api/gateway/debug-jwt` | JWT debugging information | None |

### Proxied Service Routes

| Path Pattern | Target Service | Port | Description |
|-------------|----------------|------|-------------|
| `/api/auth/**` | Auth Service | 8084 | Authentication & Authorization |
| `/api/product/**` | Product Service | 8080 | Product Management |
| `/api/order/**` | Order Service | 8081 | Order Processing |
| `/api/inventory/**` | Inventory Service | 8082 | Inventory Management |

### Request/Response Examples

**JWT Authentication Test**:
```bash
curl -X GET http://localhost:9000/api/gateway/test \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response**:
```json
{
  "status": "✅ JWT validation successful!",
  "user_id": "123",
  "username": "john.doe",
  "email": "john@example.com",
  "role": "USER",
  "expires_at": "2024-12-31T23:59:59Z",
  "issued_at": "2024-12-01T00:00:00Z",
  "algorithm": "HS384"
}
```

## 🔐 Security Implementation

### JWT Configuration

The gateway implements OAuth2 Resource Server with JWT authentication:

```kotlin
@Bean
fun jwtDecoder(): JwtDecoder {
    val keyBytes = java.util.Base64.getDecoder().decode(jwtSecret)
    val secretKey = SecretKeySpec(keyBytes, "HmacSHA384")
    
    return NimbusJwtDecoder.withSecretKey(secretKey)
        .macAlgorithm(MacAlgorithm.HS384)
        .build()
}
```

### Security Filter Chain

```kotlin
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
```

### Public Endpoints

The following endpoints are accessible without authentication:
- Swagger UI: `/swagger-ui.html`, `/swagger-ui/**`
- API Documentation: `/v3/api-docs/**`, `/aggregate/**`
- Health Checks: `/actuator/**`
- Authentication: `/api/auth/login`, `/api/auth/refresh`
- Debug Endpoints: `/api/gateway/test-no-auth`, `/api/gateway/debug-jwt`

## 🔄 Circuit Breaker Pattern

### Resilience4j Configuration

```yaml
resilience4j:
  circuitbreaker:
    configs:
      default:
        register-health-indicator: true
        sliding-window-type: COUNT_BASED
        sliding-window-size: 10
        failure-rate-threshold: 50
        wait-duration-in-open-state: 5s
        permitted-number-of-calls-in-half-open-state: 3
        automatic-transition-from-open-to-half-open-enabled: true
        minimum-number-of-calls: 5
```

### Circuit Breaker Implementation

Each service route is protected with circuit breakers:

```kotlin
@Bean
fun productServiceRoute(): RouterFunction<ServerResponse> {
    return GatewayRouterFunctions.route("product_service")
        .route(RequestPredicates.path("/api/product/**"), 
               HandlerFunctions.http(productServiceUrl))
        .filter(CircuitBreakerFilterFunctions.circuitBreaker(
            "productServiceCircuitBreaker", 
            URI.create("forward:/fallbackRoute")))
        .before { request -> addUserHeaders(request) }
        .build()
}
```

### Fallback Mechanism

```kotlin
@Bean
fun fallbackRoute(): RouterFunction<ServerResponse> {
    return GatewayRouterFunctions.route("fallbackRoute")
        .GET("/fallbackRoute") { 
            ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Service is currently unavailable. Please try again later.")
        }
        .build()
}
```

## 👤 User Context Propagation

### JWT Token Processing

The gateway extracts user information from JWT tokens and injects them as headers:

```kotlin
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
        request
    }
}
```

### User Headers Injected

| Header Name | Description | Example |
|-------------|-------------|---------|
| `X-User-Id` | Unique user identifier | `123` |
| `X-User-Email` | User email address | `john@example.com` |
| `X-User-Role` | User role/permission | `USER`, `ADMIN` |
| `X-User-Username` | Username | `john.doe` |

## 🔗 Service Integration

### Microservices Architecture

The gateway integrates with the following microservices:

```
┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   API Gateway   │
│   (Port 4200)   │◄──►│   (Port 9000)   │
└─────────────────┘    └─────────┬───────┘
                                 │
                    ┌────────────┼────────────┐
                    │            │            │
            ┌───────▼──────┐ ┌───▼────┐ ┌────▼─────┐
            │ Auth Service │ │Product │ │  Order   │
            │ (Port 8084)  │ │Service │ │ Service  │
            └──────────────┘ │(8080)  │ │ (8081)   │
                             └────────┘ └──────────┘
                                 │
                            ┌────▼─────┐
                            │Inventory │
                            │ Service  │
                            │ (8082)   │
                            └──────────┘
```

### Service Discovery

Services are configured via application properties:

```yaml
services:
  product:
    url: http://localhost:8080
  order:
    url: http://localhost:8081
  inventory:
    url: http://localhost:8082
  auth:
    url: http://localhost:8084
```

### API Documentation Aggregation

The gateway aggregates Swagger documentation from all services:

```yaml
springdoc:
  swagger-ui:
    urls:
      - name: Product Service
        url: /aggregate/product-service/v3/api-docs
      - name: Order Service
        url: /aggregate/order-service/v3/api-docs
      - name: Inventory Service
        url: /aggregate/inventory-service/v3/api-docs
```

## 🧪 Testing Strategy

### Test Structure Following Clean Architecture

```
src/test/kotlin/com/loc/api_gateway/
├── application/service/
│   ├── JwtProcessingServiceTest.kt    # JWT Processing Use Case Tests
│   └── UserHeaderServiceTest.kt       # User Header Injection Tests
├── controller/
│   └── TestControllerTest.kt          # API Integration Tests
├── infrastructure/
│   ├── adapter/
│   │   └── JwtAuthenticationAdapterTest.kt # Authentication Adapter Tests
│   ├── security/
│   │   └── SecurityConfigTest.kt      # Security Configuration Tests
│   └── routing/
│       └── RoutesTest.kt              # Gateway Routes Tests
└── integration/
    └── ApiGatewayIntegrationTest.kt   # End-to-End Integration Tests
```

### Running Tests

```bash
# Run all tests
./gradlew test

# Run specific test categories
./gradlew test --tests "*Controller*"
./gradlew test --tests "*Service*"
./gradlew test --tests "*Security*"

# Run with coverage
./gradlew test jacocoTestReport
```

### Test Technologies

- **Unit Tests**: MockK for mocking dependencies
- **Integration Tests**: Spring Boot Test with TestContainers
- **Security Tests**: Spring Security Test
- **Web Layer Tests**: @WebMvcTest with MockMvc

## 📊 Monitoring and Observability

### Health Checks

```yaml
management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: always
  health:
    circuitbreakers:
      enabled: true
```

### Available Endpoints

| Endpoint | Description |
|----------|-------------|
| `/actuator/health` | Application health status |
| `/actuator/metrics` | Application metrics |
| `/actuator/info` | Application information |
| `/actuator/circuitbreakers` | Circuit breaker status |

### Circuit Breaker Monitoring

Monitor circuit breaker states:
- **CLOSED**: Normal operation
- **OPEN**: Circuit breaker activated (service unavailable)
- **HALF_OPEN**: Testing if service is back online

## 🏆 Clean Architecture Benefits Achieved

1. **Independence**: Gateway logic is independent of frameworks and external services
2. **Testability**: Easy to test authentication and routing logic without external dependencies
3. **Flexibility**: Easy to change authentication providers or add new services
4. **Maintainability**: Clear separation of concerns makes the code easier to understand
5. **Scalability**: Architecture supports scaling individual components independently
6. **Security**: Centralized authentication and authorization with proper JWT handling
7. **Resilience**: Circuit breaker pattern ensures system stability
8. **Observability**: Comprehensive monitoring and health checks

## 🔧 Configuration Management

### Environment Variables

```bash
# JWT Secret (Base64 encoded)
SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_SECRET=MA9TFJ2cV/PxU1h68mbsq/vZB0LskxuAtsQFyZdrmBCNuGk+1pVS6YFIrQEEmnVV

# Service URLs
SERVICES_PRODUCT_URL=http://localhost:8080
SERVICES_ORDER_URL=http://localhost:8081
SERVICES_INVENTORY_URL=http://localhost:8082
SERVICES_AUTH_URL=http://localhost:8084
```

### Docker Deployment

```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app
COPY build/libs/*.jar app.jar

EXPOSE 9000
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## 📈 Performance Considerations

### Circuit Breaker Tuning

- **Sliding Window Size**: 10 calls
- **Failure Rate Threshold**: 50%
- **Wait Duration**: 5 seconds
- **Half-Open Calls**: 3 attempts

### JWT Processing

- **Algorithm**: HS384 for security
- **Token Caching**: Consider Redis for high-traffic scenarios
- **Header Injection**: Minimal overhead with direct parsing

## 🚀 Deployment Strategy

### Local Development

```bash
# Start dependencies
cd server && docker-compose up -d

# Run application
./gradlew bootRun
```

### Production Deployment

1. **Build Application**:
   ```bash
   ./gradlew build
   ```

2. **Create Docker Image**:
   ```bash
   docker build -t api-gateway:latest .
   ```

3. **Deploy with Environment Variables**:
   ```bash
   docker run -p 9000:9000 \
     -e SPRING_PROFILES_ACTIVE=prod \
     -e SERVICES_PRODUCT_URL=http://product-service:8080 \
     api-gateway:latest
   ```

This implementation demonstrates how Clean Architecture principles can be effectively applied to a Spring Boot API Gateway, resulting in a maintainable, testable, and scalable microservices gateway with proper authentication, routing, and resilience patterns. 