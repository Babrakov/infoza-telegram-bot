package ru.infoza.bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
public class InfozaBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(InfozaBotApplication.class, args);
	}

}
