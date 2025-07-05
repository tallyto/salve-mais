package com.tallyto.gestorfinanceiro.core.database;


import org.flywaydb.core.Flyway;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

@Component
public class FlywayMigrationService {
    private final DataSource dataSource;
    private final ConcurrentHashMap<String, Boolean> migratedSchemas = new ConcurrentHashMap<>();

    public FlywayMigrationService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void migrateTenantSchema(String schemaName) {
        // Evita migrações repetidas desnecessárias
        if (migratedSchemas.containsKey(schemaName)) {
            return;
        }

        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .schemas(schemaName)
                .load();

        flyway.migrate(); // Flyway já verifica se há migrações pendentes

        migratedSchemas.put(schemaName, true);
    }
}
