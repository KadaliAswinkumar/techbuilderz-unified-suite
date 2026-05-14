package com.vidyalaya.dev;

import com.vidyalaya.tenant.TenantContext;
import com.vidyalaya.tenant.TenantProvisioningService;
import com.vidyalaya.tenant.TenantRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Ensures a {@code demo} tenant exists (dev only) and loads a rich demo dataset when the tenant DB is empty.
 */
@Component
@Profile("dev")
@Order(2000)
public class DemoDataRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataRunner.class);
    private static final String DEMO_SLUG = "demo";

    private final TenantRegistry tenantRegistry;
    private final TenantProvisioningService tenantProvisioningService;
    private final DevDemoDataService devDemoDataService;

    public DemoDataRunner(
            TenantRegistry tenantRegistry,
            TenantProvisioningService tenantProvisioningService,
            DevDemoDataService devDemoDataService) {
        this.tenantRegistry = tenantRegistry;
        this.tenantProvisioningService = tenantProvisioningService;
        this.devDemoDataService = devDemoDataService;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (tenantRegistry.findBySlug(DEMO_SLUG).isEmpty()) {
            try {
                tenantProvisioningService.provision(
                        DEMO_SLUG, "Demo School", "demoadmin", "DemoAdmin123!");
                log.warn(
                        "Provisioned tenant '{}'. Tenant admin login: username=demoadmin password=DemoAdmin123!",
                        DEMO_SLUG);
            } catch (Exception e) {
                log.error("Could not auto-provision demo tenant: {}", e.getMessage());
                return;
            }
        }
        TenantContext.set(DEMO_SLUG);
        try {
            devDemoDataService.seedIfNeeded();
        } catch (Exception e) {
            log.error("Demo data seed failed: {}", e.getMessage(), e);
        } finally {
            TenantContext.clear();
        }
    }
}
