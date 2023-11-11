package ru.infoza.bot.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = {"ru.infoza.bot.repository.bot"},
        entityManagerFactoryRef = "botEntityManagerFactory",
        transactionManagerRef = "botTransactionManager"
)
public class BotDataSourceConfig {

    @Primary
    @Bean
    @ConfigurationProperties("spring.datasource.bot")
    public DataSourceProperties botDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Primary
    @Bean
    public DataSource botDataSource() {
        return botDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    @Primary
    @Bean(name = "botEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean botEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("botDataSource") DataSource dataSource) {

        return builder
                .dataSource(dataSource)
                .packages("ru.infoza.bot.model.bot")
                .persistenceUnit("bot")
                .build();
    }


    @Primary
    @Bean(name = "botTransactionManager")
    public PlatformTransactionManager botTransactionManager(
            @Qualifier("botEntityManagerFactory") EntityManagerFactory botEntityManagerFactory) {
        return new JpaTransactionManager(botEntityManagerFactory);
    }


}
