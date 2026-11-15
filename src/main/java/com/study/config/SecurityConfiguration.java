package com.study.config;

import com.study.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration {
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public PersistentTokenRepository tokenRepository(DataSource dataSource) {
        JdbcTokenRepositoryImpl repository = new JdbcTokenRepositoryImpl();
        repository.setDataSource(dataSource);
        return repository;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserService users, BCryptPasswordEncoder encoder) {
        // Explicit wiring also works when services live in the parent application context.
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(users);
        provider.setPasswordEncoder(encoder);
        return provider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, PersistentTokenRepository repository,
                                          DaoAuthenticationProvider provider, UserService users) throws Exception {
        return http
                .authenticationProvider(provider)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/static/**", "/login").permitAll()
                        .requestMatchers("/add-book", "/add-borrow", "/delete-book", "/return-book").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/doLogout").authenticated()
                        .anyRequest().authenticated())
                .formLogin(login -> login.loginPage("/login").loginProcessingUrl("/doLogin")
                        .defaultSuccessUrl("/").permitAll())
                .logout(logout -> logout.logoutUrl("/doLogout").logoutSuccessUrl("/login?logout"))
                // CSRF remains enabled; Thymeleaf th:action adds the token to POST forms.
                .rememberMe(remember -> remember.userDetailsService(users).tokenRepository(repository)
                        .tokenValiditySeconds(3600 * 24 * 7))
                .build();
    }
}
