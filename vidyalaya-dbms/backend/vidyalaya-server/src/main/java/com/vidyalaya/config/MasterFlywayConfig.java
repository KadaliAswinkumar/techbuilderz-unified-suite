package com.vidyalaya.config;

import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
public class MasterFlywayConfig {

    private static final String PASSWORD_HINT =
            """
            Master DB password rejected (PostgreSQL SQLState 28P01).
            Fix: set env MASTER_DB_USER / MASTER_DB_PASSWORD to match your Postgres (see .env.example).
            With podman/docker compose from this repo, defaults are user vidyalaya / password vidyalaya.
            If this DB volume was first created with other credentials, either update the role password in Postgres
            or remove the named volume and recreate (podman compose down -v then up -d) — that wipes local DB data.""";

    @Bean
    @DependsOn("masterDataSource")
    public Flyway masterFlyway(@Qualifier("masterDataSource") DataSource masterDataSource) {
        Flyway flyway =
                Flyway.configure()
                        .dataSource(masterDataSource)
                        .locations("classpath:db/migration/master")
                        .baselineOnMigrate(true)
                        .load();
        try {
            flyway.migrate();
        } catch (RuntimeException e) {
            if (isPasswordAuthFailure(e)) {
                throw new IllegalStateException(PASSWORD_HINT.strip(), e);
            }
            throw e;
        }
        return flyway;
    }

    /** Detects PostgreSQL invalid password (28P01) without compile dependency on driver. */
    private static boolean isPasswordAuthFailure(Throwable e) {
        for (Throwable c = e; c != null; c = c.getCause()) {
            String msg = c.getMessage();
            if (msg != null && msg.contains("password authentication failed")) {
                return true;
            }
        }
        return false;
    }
}
