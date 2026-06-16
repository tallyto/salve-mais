package br.com.salvemais;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;
import jakarta.annotation.PostConstruct;

@SpringBootApplication
@EnableScheduling
public class SalveMaisApplication {

	@PostConstruct
	public void init() {
		// Define America/Sao_Paulo como timezone padrão da aplicação
		TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"));
	}

	public static void main(String[] args) {
		SpringApplication.run(SalveMaisApplication.class, args);
	}

}
