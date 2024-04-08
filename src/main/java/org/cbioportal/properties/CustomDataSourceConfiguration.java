package org.cbioportal.properties;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class CustomDataSourceConfiguration {
    @Bean
    @ConfigurationProperties("spring.datasource.mysql")
    public DataSourceProperties mysqlDataSourceProperties() {
        return new DataSourceProperties();
    }
    
    @Bean
    @ConfigurationProperties("spring.datasource.columnar")
    public DataSourceProperties columnarDatSourceProperties() {
        return new DataSourceProperties();
    }
    
    @Bean
    @Qualifier("mysqlDataSource")
    public DataSource mysqlDataSource() {
        return mysqlDataSourceProperties()
            .initializeDataSourceBuilder()
            .build();
    }
    
    @Bean
    public DataSource columnarDataSource() {
        return columnarDatSourceProperties()
            .initializeDataSourceBuilder()
            .build();
    }
}
