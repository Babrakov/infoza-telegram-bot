package ru.infoza.bot.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

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

    @Bean
    public DataSource postgresDataSource() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(postgresUrl);
        hikariConfig.setUsername(postgresUser);
        hikariConfig.setPassword(postgresPassword);

        return new HikariDataSource(hikariConfig);
    }

}
