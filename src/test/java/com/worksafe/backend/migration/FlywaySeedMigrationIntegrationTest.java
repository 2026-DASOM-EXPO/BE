package com.worksafe.backend.migration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:flyway-seed;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true",
        "spring.flyway.baseline-on-migrate=false"
})
@ActiveProfiles("test")
class FlywaySeedMigrationIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void createsSchemaAndSeedsFiveRowsPerTableWithFiveEquipmentSets() {
        assertThat(count("users")).isEqualTo(5);
        assertThat(count("refresh_tokens")).isEqualTo(5);
        assertThat(count("workers")).isEqualTo(5);
        assertThat(count("equipment")).isEqualTo(15);
        assertThat(count("equipment_logs")).isEqualTo(5);
        assertThat(count("wearable_commands")).isEqualTo(5);
        assertThat(count("sensor_logs")).isEqualTo(5);
        assertThat(count("risk_events")).isEqualTo(5);
        assertThat(count("alerts")).isEqualTo(5);
        assertThat(count("drones")).isEqualTo(5);
        assertThat(count("drone_dispatches")).isEqualTo(5);
        assertThat(count("drone_videos")).isEqualTo(5);
        assertThat(count("drone_drop_logs")).isEqualTo(5);

        Integer workersWithFullSet = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM (
                    SELECT worker_id
                    FROM equipment
                    WHERE type IN ('HELMET', 'VEST', 'SHOES')
                    GROUP BY worker_id
                    HAVING COUNT(DISTINCT type) = 3
                ) equipment_sets
                """, Integer.class);
        assertThat(workersWithFullSet).isEqualTo(5);
    }

    @Test
    void seededManagerPasswordCanBeUsedForLogin() {
        String encodedPassword = jdbcTemplate.queryForObject(
                "SELECT password FROM users WHERE login_id = 'manager'",
                String.class
        );

        assertThat(passwordEncoder.matches("Admin123!", encodedPassword)).isTrue();
    }

    private long count(String tableName) {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Long.class);
        return count == null ? 0 : count;
    }
}
