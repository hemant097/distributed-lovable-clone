package com.example.distributed_lovable_clone.api_gateway.filter;

import com.example.distributed_lovable_clone.api_gateway.error.APIError;
import com.example.distributed_lovable_clone.api_gateway.properties.SecurityProperties;
import com.example.distributed_lovable_clone.api_gateway.service.JwtGatewayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

@Component
@Slf4j
@RequiredArgsConstructor
public class GatewayJwtAuthFilter implements GlobalFilter, Ordered {

    private final SecurityProperties securityProperties;
    private final JwtGatewayService jwtGatewayService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher(); //using contains can produce false positives
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        boolean isPublic = securityProperties.getPublicRoutes().stream()
                .anyMatch(pattern -> pathMatcher.match(pattern,path));

        if(isPublic){
            log.info("Public route, continue: {}",path);
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst("Authorization");
        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            log.error("Missing or invalid Authorization header for path: {}",path);
            return sendErrorResponse(exchange,HttpStatus.UNAUTHORIZED,"Missing/Invalid authorization header");
        }

        String token = authHeader.substring(7);

        try{
            jwtGatewayService.validateToken(token);
            log.info("JWT token valid for path: {}",path);
        }catch (Exception ex){
            log.error("JWT validation failed at Gateway:{}",ex.getMessage());
            return sendErrorResponse(exchange,HttpStatus.UNAUTHORIZED,ex.getMessage());
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1;
    }

    Mono<Void> sendErrorResponse(ServerWebExchange exchange,HttpStatus status, String message){
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().add("Content-Type","application/json");

        APIError apiError = new APIError(status,message);
        try{
            byte[] bytes = objectMapper.writeValueAsBytes(apiError);
            DataBuffer dataBuffer = exchange.getResponse().bufferFactory().wrap(bytes);
            return exchange.getResponse().writeWith(Mono.just(dataBuffer));
        }
        catch (Exception e){
            log.error("Error serializing gateway error response ",e);
            return exchange.getResponse().setComplete();
        }

    }
}
