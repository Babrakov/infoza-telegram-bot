package ru.infoza.simplebot.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
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
        basePackages = {"ru.infoza.simplebot.repository.info"},
        entityManagerFactoryRef = "infoEntityManagerFactory",
        transactionManagerRef = "infoTransactionManager"
)
public class InfoDataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.info")
    public DataSourceProperties infoDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource infoDataSource() {
        return infoDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean(name = "infoEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean infoEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("infoDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("ru.infoza.simplebot.model.info")
                .persistenceUnit("info")
                .build();
    }

    @Bean(name = "infoTransactionManager")
    public PlatformTransactionManager infoTransactionManager(
            @Qualifier("infoEntityManagerFactory") EntityManagerFactory infoEntityManagerFactory) {
        return new JpaTransactionManager(infoEntityManagerFactory);
    }


}
