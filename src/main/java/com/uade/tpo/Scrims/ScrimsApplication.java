package com.uade.tpo.Scrims;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ScrimsApplication {

	public static void main(String[] args) {
		SpringApplication.run(ScrimsApplication.class, args);
		System.out.println("Server is running on port 8080");
	}

}
