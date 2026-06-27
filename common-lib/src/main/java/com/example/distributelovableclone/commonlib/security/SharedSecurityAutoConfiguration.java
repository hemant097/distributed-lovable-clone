package com.example.distributelovableclone.commonlib.security;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerExceptionResolver;

@AutoConfiguration
public class SharedSecurityAutoConfiguration {

    @Bean
    public AuthUtil authUtil(){
        return new AuthUtil();
    }

    @Bean
    public JwtAuthFilter jwtAuthFilter(AuthUtil authUtil,
                                       HandlerExceptionResolver handlerExceptionResolver){
        return new JwtAuthFilter(authUtil,handlerExceptionResolver);
    }

    //passing the jwt token to the downstream service, so that it can verify jwt itself
    @Bean
    public RequestInterceptor requestInterceptor(){
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate requestTemplate) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

                if(authentication!=null && authentication.getCredentials() instanceof String token)
                    requestTemplate.header("Authorization","Bearer "+token);
            }
        };
    }

}
