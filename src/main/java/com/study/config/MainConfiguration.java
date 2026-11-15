package com.study.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@ComponentScan("com.study.service")
@MapperScan("com.study.mapper")
@EnableTransactionManagement
@PropertySource("classpath:application.properties")
@Import(ValidationConfiguration.class)
public class MainConfiguration {
    @Bean(destroyMethod = "close")
    public HikariDataSource dataSource(Environment environment) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(environment.getRequiredProperty("DB_URL"));
        config.setUsername(environment.getRequiredProperty("DB_USER"));
        config.setPassword(environment.getRequiredProperty("DB_PASSWORD"));
        config.setMaximumPoolSize(10);
        return new HikariDataSource(config);
    }

    @Bean
    public SqlSessionFactoryBean sqlSessionFactoryBean(DataSource dataSource) {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        return bean;
    }

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
