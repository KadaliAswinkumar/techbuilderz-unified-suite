package com.vidyalaya.tenant;

import com.vidyalaya.crypto.CryptoService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.net.URI;
import java.sql.Connection;
import java.sql.Statement;
import java.util.UUID;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class TenantProvisioningService {

    private final DataSource masterDataSource;
    private final TenantRegistry tenantRegistry;
    private final CryptoService cryptoService;
    private final PasswordEncoder passwordEncoder;
    private final String masterUrl;
    private final String masterUser;
    private final String masterPassword;

    public TenantProvisioningService(
            @Qualifier("masterDataSource") DataSource masterDataSource,
            TenantRegistry tenantRegistry,
            CryptoService cryptoService,
            PasswordEncoder passwordEncoder,
            @Value("${vidyalaya.master-datasource.url}") String masterUrl,
            @Value("${vidyalaya.master-datasource.username}") String masterUser,
            @Value("${vidyalaya.master-datasource.password}") String masterPassword) {
        this.masterDataSource = masterDataSource;
        this.tenantRegistry = tenantRegistry;
        this.cryptoService = cryptoService;
        this.passwordEncoder = passwordEncoder;
        this.masterUrl = masterUrl;
        this.masterUser = masterUser;
        this.masterPassword = masterPassword;
    }

    public TenantRecord provision(
            String slug, String schoolName, String adminUsername, String adminPassword) {
        if (tenantRegistry.findBySlug(slug).isPresent()) {
            throw new IllegalArgumentException("Tenant slug already exists");
        }
        String dbName = sanitizeDbName(slug);
        createDatabase(dbName);
        String jdbcUrl = toJdbcUrl(dbName);
        runFlywayOnNewDb(jdbcUrl);
        String encPass = cryptoService.encrypt(masterPassword);
        UUID id = UUID.randomUUID();
        TenantRecord record =
                new TenantRecord(
                        id,
                        slug,
                        schoolName,
                        dbName,
                        hostFromJdbc(masterUrl),
                        portFromJdbc(masterUrl),
                        masterUser,
                        encPass,
                        "#4F46E5",
                        null,
                        null,
                        java.time.Instant.now());
        tenantRegistry.insert(record);
        insertAdminUser(jdbcUrl, adminUsername, adminPassword);
        return record;
    }

    private void insertAdminUser(String jdbcUrl, String adminUsername, String adminPassword) {
        HikariDataSource ds = buildPool(jdbcUrl, 1);
        try (var conn = ds.getConnection();
                var ps =
                        conn.prepareStatement(
                                """
                                INSERT INTO app_users (id, username, password_hash, role, active)
                                VALUES (?,?,?,?,true)
                                """)) {
            ps.setObject(1, UUID.randomUUID());
            ps.setString(2, adminUsername);
            ps.setString(3, passwordEncoder.encode(adminPassword));
            ps.setString(4, "ADMIN");
            ps.executeUpdate();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to seed tenant admin", e);
        } finally {
            ds.close();
        }
    }

    private void runFlywayOnNewDb(String jdbcUrl) {
        Flyway.configure()
                .dataSource(jdbcUrl, masterUser, masterPassword)
                .locations("classpath:db/migration/tenant")
                .baselineOnMigrate(true)
                .load()
                .migrate();
    }

    private void createDatabase(String dbName) {
        String adminUrl = masterUrl.replaceAll("/[^/?]+$", "/postgres");
        HikariDataSource ds = buildPool(adminUrl, 1);
        try (Connection c = ds.getConnection();
                Statement st = c.createStatement()) {
            c.setAutoCommit(true);
            st.execute("CREATE DATABASE " + dbName);
        } catch (Exception e) {
            throw new IllegalStateException("CREATE DATABASE failed: " + dbName, e);
        } finally {
            ds.close();
        }
    }

    private HikariDataSource buildPool(String url, int max) {
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(url);
        cfg.setUsername(masterUser);
        cfg.setPassword(masterPassword);
        cfg.setMaximumPoolSize(max);
        return new HikariDataSource(cfg);
    }

    private String toJdbcUrl(String dbName) {
        return masterUrl.replaceAll("/[^/?]+$", "/" + dbName);
    }

    static String sanitizeDbName(String slug) {
        String s = slug.toLowerCase().replaceAll("[^a-z0-9]", "_");
        if (s.isBlank()) {
            s = "school";
        }
        if (!s.startsWith("t_")) {
            s = "t_" + s;
        }
        if (s.length() > 60) {
            s = s.substring(0, 60);
        }
        return s;
    }

    static String hostFromJdbc(String jdbcUrl) {
        String u = jdbcUrl.replace("jdbc:postgresql://", "http://");
        URI uri = URI.create(u);
        return uri.getHost();
    }

    static int portFromJdbc(String jdbcUrl) {
        String u = jdbcUrl.replace("jdbc:postgresql://", "http://");
        URI uri = URI.create(u);
        int p = uri.getPort();
        return p > 0 ? p : 5432;
    }
}
