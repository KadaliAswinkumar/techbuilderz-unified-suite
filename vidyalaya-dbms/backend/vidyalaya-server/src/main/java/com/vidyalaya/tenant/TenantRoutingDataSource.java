package com.vidyalaya.tenant;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.AbstractDataSource;

public class TenantRoutingDataSource extends AbstractDataSource {

    private final TenantDataSourceFactory factory;

    public TenantRoutingDataSource(TenantDataSourceFactory factory) {
        this.factory = factory;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return resolve().getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return resolve().getConnection(username, password);
    }

    private DataSource resolve() {
        String slug = TenantContext.require();
        return factory.forSlug(slug);
    }

    /** For Spring Boot health / introspection only */
    public Map<String, DataSource> targetDataSources() {
        return Map.of();
    }
}
