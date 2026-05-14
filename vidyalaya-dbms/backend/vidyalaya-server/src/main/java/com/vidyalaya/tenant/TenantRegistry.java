package com.vidyalaya.tenant;

import com.vidyalaya.crypto.CryptoService;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class TenantRegistry {

    private final JdbcTemplate masterJdbc;
    private final CryptoService cryptoService;

    public TenantRegistry(
            @Qualifier("masterJdbcTemplate") JdbcTemplate masterJdbcTemplate, CryptoService cryptoService) {
        this.masterJdbc = masterJdbcTemplate;
        this.cryptoService = cryptoService;
    }

    private static final RowMapper<TenantRecord> MAPPER =
            new RowMapper<>() {
                @Override
                public TenantRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return new TenantRecord(
                            rs.getObject("id", UUID.class),
                            rs.getString("slug"),
                            rs.getString("name"),
                            rs.getString("db_name"),
                            rs.getString("db_host"),
                            rs.getInt("db_port"),
                            rs.getString("db_username"),
                            rs.getString("db_password_enc"),
                            rs.getString("primary_color"),
                            rs.getString("logo_url"),
                            rs.getString("openai_api_key_enc"),
                            rs.getTimestamp("created_at").toInstant());
                }
            };

    public Optional<TenantRecord> findBySlug(String slug) {
        return masterJdbc
                .query(
                        "SELECT id, slug, name, db_name, db_host, db_port, db_username, db_password_enc, primary_color, logo_url, openai_api_key_enc, created_at FROM tenants WHERE slug = ?",
                        MAPPER,
                        slug)
                .stream()
                .findFirst();
    }

    public void insert(TenantRecord r) {
        masterJdbc.update(
                """
                INSERT INTO tenants (id, slug, name, db_name, db_host, db_port, db_username, db_password_enc, primary_color, logo_url, openai_api_key_enc)
                VALUES (?,?,?,?,?,?,?,?,?,?,?)
                """,
                r.id(),
                r.slug(),
                r.name(),
                r.dbName(),
                r.dbHost(),
                r.dbPort(),
                r.dbUsername(),
                r.dbPasswordEnc(),
                r.primaryColor(),
                r.logoUrl(),
                r.openaiApiKeyEnc());
    }

    public String decryptDbPassword(TenantRecord r) {
        return cryptoService.decrypt(r.dbPasswordEnc());
    }

    public Optional<String> decryptOpenAiKey(TenantRecord r) {
        if (r.openaiApiKeyEnc() == null || r.openaiApiKeyEnc().isBlank()) {
            return Optional.empty();
        }
        return Optional.of(cryptoService.decrypt(r.openaiApiKeyEnc()));
    }

    public void updateBranding(String slug, String primaryColor, String logoUrl) {
        masterJdbc.update(
                "UPDATE tenants SET primary_color = ?, logo_url = ? WHERE slug = ?",
                primaryColor,
                logoUrl,
                slug);
    }

    public void updateOpenAiKey(String slug, String openAiApiKeyPlain) {
        String enc = openAiApiKeyPlain == null || openAiApiKeyPlain.isBlank() ? null : cryptoService.encrypt(openAiApiKeyPlain);
        masterJdbc.update("UPDATE tenants SET openai_api_key_enc = ? WHERE slug = ?", enc, slug);
    }
}
