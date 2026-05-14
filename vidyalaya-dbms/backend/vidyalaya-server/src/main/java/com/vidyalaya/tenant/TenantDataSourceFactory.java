package com.vidyalaya.tenant;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class TenantDataSourceFactory {

    private final TenantRegistry tenantRegistry;
    private final Map<String, DataSource> cache = new ConcurrentHashMap<>();

    public TenantDataSourceFactory(@Lazy TenantRegistry tenantRegistry) {
        this.tenantRegistry = tenantRegistry;
    }

    public DataSource forSlug(String slug) {
        return cache.computeIfAbsent(slug, this::build);
    }

    private DataSource build(String slug) {
        TenantRecord t =
                tenantRegistry
                        .findBySlug(slug)
                        .orElseThrow(() -> new IllegalArgumentException("Unknown tenant: " + slug));
        String password = tenantRegistry.decryptDbPassword(t);
        String jdbcUrl =
                "jdbc:postgresql://"
                        + t.dbHost()
                        + ":"
                        + t.dbPort()
                        + "/"
                        + t.dbName();
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(jdbcUrl);
        cfg.setUsername(t.dbUsername());
        cfg.setPassword(password);
        cfg.setPoolName("tenant-" + slug);
        cfg.setMaximumPoolSize(10);
        return new HikariDataSource(cfg);
    }

    public void evict(String slug) {
        DataSource ds = cache.remove(slug);
        if (ds instanceof HikariDataSource h) {
            h.close();
        }
    }
}
