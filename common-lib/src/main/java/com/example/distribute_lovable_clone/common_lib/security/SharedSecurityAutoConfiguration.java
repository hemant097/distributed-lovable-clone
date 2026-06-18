package com.example.distribute_lovable_clone.common_lib.security;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.HandlerExceptionResolver;

@AutoConfiguration
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

}
