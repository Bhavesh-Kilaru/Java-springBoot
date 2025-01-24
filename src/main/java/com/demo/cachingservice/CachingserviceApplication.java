package com.demo.cachingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "Caching Service API", version = "1.0", description = "API for caching service with database eviction"))
@EnableJpaRepositories
public class CachingserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CachingserviceApplication.class, args);
	}

}
