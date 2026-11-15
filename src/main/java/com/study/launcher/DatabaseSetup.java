package com.study.launcher;

import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/** Explicit, fresh-database initialization; never runs automatically at application startup. */
public final class DatabaseSetup {
    private DatabaseSetup() {}

    public static void main(String[] args) {
        String password = required("DB_PASSWORD");
        String adminPassword = required("ADMIN_PASSWORD");
        if (adminPassword.length() < 12) {
            throw new IllegalArgumentException("ADMIN_PASSWORD must contain at least 12 characters");
        }
        var source = new DriverManagerDataSource(required("DB_URL"), required("DB_USER"), password);
        var jdbc = new JdbcTemplate(source);
        // Refuse to overwrite any existing application tables or seed data.
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name IN ('book','student','student2','borrow','user','persistent_logins')", Integer.class);
        if (count == null || count != 0) {
            throw new IllegalStateException("Database is not empty; initialization refused. Review a migration instead.");
        }
        new ResourceDatabasePopulator(new ClassPathResource("db/schema.sql"), new ClassPathResource("db/demo-data.sql")).execute(source);
        var encoder = new BCryptPasswordEncoder();
        jdbc.update("INSERT INTO user(username,password,role) VALUES(?,?,?)", "admin", encoder.encode(adminPassword), "ADMIN");
        System.out.println("Fresh database initialized; admin account created.");
    }

    private static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Missing environment variable: " + name);
        return value;
    }
}
