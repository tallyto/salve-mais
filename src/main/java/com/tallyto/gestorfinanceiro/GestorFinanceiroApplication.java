package com.tallyto.gestorfinanceiro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;
import jakarta.annotation.PostConstruct;

@SpringBootApplication
@EnableScheduling
public class GestorFinanceiroApplication {

	@PostConstruct
	public void init() {
		// Define UTC como timezone padrão da aplicação
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}

	public static void main(String[] args) {
		SpringApplication.run(GestorFinanceiroApplication.class, args);
	}

}
