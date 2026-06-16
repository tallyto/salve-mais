package br.com.salvemais.infrastructure.context;


import org.slf4j.MDC;

import java.util.Objects;
import java.util.function.Supplier;

public class TenantContext {
    private static final String LOGGER_TENANT_ID = "tenant_id";
    public static final String DEFAULT_TENANT = "public";

    private static final ThreadLocal<String> currentTenant = new InheritableThreadLocal<>();

    public static String getCurrentTenant() {
        String tenant = currentTenant.get();
        return Objects.requireNonNullElse(tenant, DEFAULT_TENANT);
    }

    public static void setCurrentTenant(String tenant) {
        MDC.put(LOGGER_TENANT_ID, tenant);
        currentTenant.set(tenant);
    }

    public static void clear() {
        MDC.clear();
        currentTenant.remove();
    }

    public static <T> T withTenant(String tenant, Supplier<T> supplier) {
        String previousTenant = getCurrentTenant();
        try {
            setCurrentTenant(tenant);
            return supplier.get();
        } finally {
            if (previousTenant == null || DEFAULT_TENANT.equals(previousTenant)) {
                clear();
            } else {
                setCurrentTenant(previousTenant);
            }
        }
    }

    public static void runWithTenant(String tenant, Runnable runnable) {
        withTenant(tenant, () -> {
            runnable.run();
            return null;
        });
    }
}
