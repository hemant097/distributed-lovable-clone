package com.example.distribute_lovable_clone.api_gateway;

import com.example.distribute_lovable_clone.common_lib.errors.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
        GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();
	}

}
