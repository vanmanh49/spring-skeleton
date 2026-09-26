package com.vm.skeleton.migration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;
import java.util.Map;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

/**
 * Migrates a V1 schema holding per-user role rows to V2 and checks the data ends up in {@code roles}/{@code user_roles}.
 */
class V2NormalizeRolesMigrationTest {

    @Test
    void v2_movesLegacyRoleRowsIntoSharedRolesAndUserRoles() {
        SimpleDriverDataSource dataSource = new SimpleDriverDataSource(new org.h2.Driver(),
                "jdbc:h2:mem:v2migration;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1", "sa", "");
        JdbcClient jdbc = JdbcClient.create(dataSource);

        Flyway.configure().dataSource(dataSource).target("1").load().migrate();
        jdbc.sql("INSERT INTO users (id, user_name, hashed_password, created_at) VALUES "
                + "(1, 'alice', 'x', CURRENT_TIMESTAMP), (2, 'bob', 'x', CURRENT_TIMESTAMP)").update();
        jdbc.sql("INSERT INTO roles (role_code, user_id) VALUES "
                + "('ADMINISTRATOR', 1), ('EDITOR', 1), ('EDITOR', 1), ('AUDITOR', 2)").update();

        Flyway.configure().dataSource(dataSource).load().migrate();

        assertThat(jdbc.sql("SELECT code FROM roles").query(String.class).list())
                .containsExactlyInAnyOrder("ADMINISTRATOR", "EDITOR", "AUDITOR");
        List<Map<String, Object>> userRoles = jdbc.sql(
                "SELECT ur.user_id AS user_id, r.code AS code FROM user_roles ur JOIN roles r ON r.id = ur.role_id")
                .query().listOfRows();
        assertThat(userRoles)
                .extracting(row -> ((Number) row.get("user_id")).longValue(), row -> row.get("code"))
                .containsExactlyInAnyOrder(tuple(1L, "ADMINISTRATOR"), tuple(1L, "EDITOR"), tuple(2L, "AUDITOR"));
    }
}
