package com.vidyalaya.config;

import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class MasterDataSourceConfig {

    @Bean(name = "masterDataSource")
    public DataSource masterDataSource(
            @Value("${vidyalaya.master-datasource.url}") String url,
            @Value("${vidyalaya.master-datasource.username}") String username,
            @Value("${vidyalaya.master-datasource.password}") String password) {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setPoolName("master-pool");
        ds.setMaximumPoolSize(5);
        return ds;
    }

    @Bean(name = "masterJdbcTemplate")
    public JdbcTemplate masterJdbcTemplate(@Qualifier("masterDataSource") DataSource masterDataSource) {
        return new JdbcTemplate(masterDataSource);
    }
}
