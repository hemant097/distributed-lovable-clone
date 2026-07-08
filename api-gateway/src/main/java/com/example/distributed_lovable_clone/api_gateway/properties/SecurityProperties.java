package com.example.distributed_lovable_clone.api_gateway.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@ConfigurationProperties(prefix = "app.security")
@Component
@Getter@Setter
public class SecurityProperties {

    private List<String> publicRoutes;
}
