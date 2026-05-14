package com.vidyalaya.config;

import com.vidyalaya.tenant.TenantDataSourceFactory;
import com.vidyalaya.tenant.TenantRoutingDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TenantBeansConfig {

    @Bean
    public TenantRoutingDataSource tenantRoutingDataSource(TenantDataSourceFactory factory) {
        return new TenantRoutingDataSource(factory);
    }
}
