package com.flyway.auth.integration;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@MapperScan("com.flyway.auth.mapper")
public class MyBatisTestConfig {

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("org.h2.Driver");
        ds.setUrl("jdbc:h2:mem:flyway_test;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false");
        ds.setUsername("sa");
        ds.setPassword("");
        return ds;
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setMapperLocations(
                new PathMatchingResourcePatternResolver().getResources("classpath*:mapper/**/*.xml")
        );
        org.apache.ibatis.session.Configuration mybatisConfiguration =
                new org.apache.ibatis.session.Configuration();
        mybatisConfiguration.setMapUnderscoreToCamelCase(true);
        factory.setConfiguration(mybatisConfiguration);
        return factory.getObject();
    }

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public DataSourceInitializer dataSourceInitializer(DataSource dataSource) {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ByteArrayResource(schemaSql().getBytes()));
        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(dataSource);
        initializer.setDatabasePopulator(populator);
        return initializer;
    }

    private String schemaSql() {
        return ""
                + "CREATE TABLE signup_attempt ("
                + " attempt_id VARCHAR(36) PRIMARY KEY,"
                + " email VARCHAR(255) NOT NULL,"
                + " status VARCHAR(20) NOT NULL,"
                + " created_at TIMESTAMP NOT NULL,"
                + " expires_at TIMESTAMP NOT NULL,"
                + " verified_at TIMESTAMP NULL,"
                + " consumed_at TIMESTAMP NULL"
                + ");"
                + "CREATE TABLE email_verification_token ("
                + " email_verification_token_id VARCHAR(36) PRIMARY KEY,"
                + " email VARCHAR(255) NOT NULL,"
                + " purpose VARCHAR(50) NOT NULL,"
                + " token_hash VARCHAR(255) NOT NULL,"
                + " expires_at TIMESTAMP NOT NULL,"
                + " used_at TIMESTAMP NULL,"
                + " created_at TIMESTAMP NOT NULL,"
                + " attempt_id VARCHAR(36) NOT NULL"
                + ");"
                + "CREATE UNIQUE INDEX uq_email_verification_token_hash"
                + " ON email_verification_token(token_hash);"
                + "CREATE INDEX idx_email_verification_attempt"
                + " ON email_verification_token(attempt_id);"
                + "CREATE INDEX idx_signup_attempt_email"
                + " ON signup_attempt(email);";
    }
}
