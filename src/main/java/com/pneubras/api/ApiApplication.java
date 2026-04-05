package com.pneubras.api;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.pneubras.api.service.ApiService;

import lombok.RequiredArgsConstructor;

@SpringBootApplication
@RequiredArgsConstructor
public class ApiApplication implements CommandLineRunner {

	private final ApiService apiService;

    @Override
    public void run(String... args) throws Exception {
		System.out.println("API is running");

		apiService.bootstrap();
    }

	public static void main(String[] args) {
		SpringApplication.run(ApiApplication.class, args);
	}

}
