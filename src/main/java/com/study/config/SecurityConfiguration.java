package com.study.config;


import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.sql.DataSource;


@EnableWebSecurity
@Configuration
public class SecurityConfiguration {


    @Bean
    public BCryptPasswordEncoder passwordEncoder() {

        System.out.println(new BCryptPasswordEncoder().encode("password"));
        return new BCryptPasswordEncoder();
    }

    /*
    @Bean
    public SqlSessionFactoryBean sqlSessionFactoryBean(DataSource datasource){
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(datasource);
        return bean;
    }*/

    @Bean
    public PersistentTokenRepository tokenRepository(DataSource dataSource) {
        JdbcTokenRepositoryImpl repository = new JdbcTokenRepositoryImpl();
        repository.setDataSource(dataSource);
        //repository.setCreateTableOnStartup(true);
        return repository;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           PersistentTokenRepository repository) throws Exception {

        return http
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/static/**").permitAll();
                    auth.anyRequest().authenticated();

                })
                .formLogin(httpSecurityFormLoginConfigurer -> {
                    httpSecurityFormLoginConfigurer.loginPage("/login");
                    httpSecurityFormLoginConfigurer.loginProcessingUrl("/doLogin");
                    httpSecurityFormLoginConfigurer.defaultSuccessUrl("/");
                    httpSecurityFormLoginConfigurer.permitAll();
                    httpSecurityFormLoginConfigurer.usernameParameter("username");
                    httpSecurityFormLoginConfigurer.passwordParameter("password");
                })
                .logout(httpSecurityLogoutConfigurer -> {
                    httpSecurityLogoutConfigurer.logoutUrl("/doLogout");
                    httpSecurityLogoutConfigurer.logoutSuccessUrl("/login");
                    httpSecurityLogoutConfigurer.permitAll();
                })
                .csrf(AbstractHttpConfigurer::disable)
                .rememberMe(httpSecurityRememberMeConfigurer -> {
                    httpSecurityRememberMeConfigurer.tokenRepository(repository);
                    httpSecurityRememberMeConfigurer.tokenValiditySeconds(3600 * 24 * 7);
                })
                .build();

    }


}
