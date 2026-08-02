package de.digidrivelog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@SpringBootApplication
public class DigidrivelogApplication {
	public static void main(String[] args) {
		SpringApplication.run(DigidrivelogApplication.class, args);
	}
	// This will be removed once we have a proper frontend, it's just here to verify that the backend is running correctly.
	@RestController
	public static class HelloController {
		@GetMapping("/")
		public String hello() {
			return "Hello, Digidrivelog!";
		}
	}
}
