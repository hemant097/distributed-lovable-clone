package com.example.distributelovableclone.commonlib.security;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerExceptionResolver;

@AutoConfiguration
@Slf4j
public class SharedSecurityAutoConfiguration {

    @Bean
    public AuthUtil authUtil(){
        return new AuthUtil();
    }

    @Bean
    public JwtAuthFilter jwtAuthFilter(AuthUtil authUtil,
                                       @Qualifier("handlerExceptionResolver")
                                       HandlerExceptionResolver handlerExceptionResolver){
        return new JwtAuthFilter(authUtil,handlerExceptionResolver);
    }

    //passing the jwt token to the downstream service, so that it can verify jwt itself (eg. when a feignClient is used to call another service)
    @Bean
    public RequestInterceptor requestInterceptor(){
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate requestTemplate) {
                log.info("Thread: {}", Thread.currentThread().getName());
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

                log.info("Authentication: {}", authentication);
                log.info("Context: {}", SecurityContextHolder.getContext());

                if (requestTemplate.headers().containsKey("Authorization")) {
                    return;
                }

                if(authentication!=null && authentication.getCredentials() instanceof String token)
                    requestTemplate.header("Authorization","Bearer "+token);

                log.info("Headers: {}", requestTemplate.headers());
            }
        };
    }

}
