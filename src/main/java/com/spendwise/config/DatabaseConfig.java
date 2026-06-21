package com.spendwise.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

@Configuration
@Profile("prod")
public class DatabaseConfig {

    private static final Logger LOGGER = Logger.getLogger(DatabaseConfig.class.getName());

    @Value("${DATABASE_URL:}")
    private String databaseUrl;

    @Bean
    public DataSource dataSource() throws URISyntaxException {
        if (databaseUrl == null || databaseUrl.isEmpty()) {
            throw new IllegalArgumentException("DATABASE_URL environment variable is not set!");
        }

        LOGGER.info("Parsing DATABASE_URL for PostgreSQL configuration...");
        URI dbUri = new URI(databaseUrl);
        
        String username = "";
        String password = "";
        if (dbUri.getUserInfo() != null) {
            String[] userInfo = dbUri.getUserInfo().split(":");
            username = userInfo[0];
            if (userInfo.length > 1) {
                password = userInfo[1];
            }
        }

        String port = dbUri.getPort() != -1 ? ":" + dbUri.getPort() : "";
        String query = dbUri.getQuery() != null ? "?" + dbUri.getQuery() : "";
        
        // Build valid JDBC PostgreSQL URL preserving paths and query parameters (e.g. SSL options)
        String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + port + dbUri.getPath() + query;

        return DataSourceBuilder.create()
                .url(dbUrl)
                .username(username)
                .password(password)
                .driverClassName("org.postgresql.Driver")
                .build();
    }
}
