package ru.infoza.bot.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Data
@PropertySource("classpath:application.properties")
public class PostgresConfig {

    @Value("${infoza.postgres.url}")
    String postgresUrl;

    @Value("${infoza.postgres.username}")
    String postgresUser;

    @Value("${infoza.postgres.password}")
    String postgresPassword;
}
