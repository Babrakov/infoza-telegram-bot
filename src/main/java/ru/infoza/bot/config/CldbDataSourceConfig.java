package ru.infoza.bot.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
        basePackages = {"ru.infoza.bot.repository.cldb"},
        entityManagerFactoryRef = "cldbEntityManagerFactory",
        transactionManagerRef = "cldbTransactionManager"
)
public class CldbDataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.cldb")
    public DataSourceProperties cldbDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource cldbDataSource() {
        return cldbDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean(name = "cldbEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean cldbEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("cldbDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("ru.infoza.bot.model.cldb")
                .persistenceUnit("cldb")
                .build();
    }

    @Bean(name = "cldbTransactionManager")
    public PlatformTransactionManager cldbTransactionManager(
            @Qualifier("cldbEntityManagerFactory") EntityManagerFactory cldbEntityManagerFactory) {
        return new JpaTransactionManager(cldbEntityManagerFactory);
    }


}
