package com.vidyalaya.config;

import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Logs how the API is configured at startup (no secrets) so local issues (DB down, wrong URL) are obvious.
 */
@Component
@Order(0)
public class StartupDiagnostics implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupDiagnostics.class);

    private final DataSource masterDataSource;
    private final String masterUrl;
    private final String corsOrigins;

    public StartupDiagnostics(
            @Qualifier("masterDataSource") DataSource masterDataSource,
            @Value("${vidyalaya.master-datasource.url}") String masterUrl,
            @Value(
                    "${vidyalaya.cors.allowed-origins:http://localhost:5173,http://127.0.0.1:5173,http://[::1]:5173}")
                    String corsOrigins) {
        this.masterDataSource = masterDataSource;
        this.masterUrl = masterUrl;
        this.corsOrigins = corsOrigins;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("Vidyalaya API is up. Master DB URL: {}", masterUrl);
        log.info("CORS allowed origins: {}", corsOrigins);
        try (var c = masterDataSource.getConnection()) {
            log.info(
                    "Master PostgreSQL connection OK (catalog={}, readOnly={})",
                    c.getCatalog(),
                    c.isReadOnly());
        } catch (Exception e) {
            log.error(
                    "Master PostgreSQL is NOT reachable. Start the database first, e.g. `podman compose up -d`"
                        + " from the project root, then restart the API. Underlying error:",
                    e);
        }
    }
}
