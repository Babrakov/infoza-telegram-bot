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
        basePackages = {"ru.infoza.bot.repository.infoza"},
        entityManagerFactoryRef = "infozaEntityManagerFactory",
        transactionManagerRef = "infozaTransactionManager"
)
public class InfozaDataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.infoza")
    public DataSourceProperties infozaDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource infozaDataSource() {
        return infozaDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean(name = "infozaEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean infozaEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("infozaDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("ru.infoza.bot.model.infoza")
                .persistenceUnit("infoza")
                .build();
    }

    @Bean(name = "infozaTransactionManager")
    public PlatformTransactionManager infozaTransactionManager(
            @Qualifier("infozaEntityManagerFactory") EntityManagerFactory infozaEntityManagerFactory) {
        return new JpaTransactionManager(infozaEntityManagerFactory);
    }


}
