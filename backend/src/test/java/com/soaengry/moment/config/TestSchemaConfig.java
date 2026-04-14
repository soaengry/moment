package com.soaengry.moment.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 테스트 DB 스키마 보정: ddl-auto=update는 기존 NOT NULL 컬럼을 nullable로 바꾸지 않으므로
 * 엔티티가 nullable을 허용하는 컬럼을 수동으로 수정한다.
 */
@TestConfiguration
public class TestSchemaConfig {

    @Bean
    public TestSchemaFixer testSchemaFixer(DataSource dataSource) {
        return new TestSchemaFixer(dataSource);
    }

    public static class TestSchemaFixer {
        public TestSchemaFixer(DataSource dataSource) {
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                // Event 테이블 location 컬럼을 nullable로 변경
                executeIgnoreError(stmt, "ALTER TABLE events MODIFY COLUMN location_name VARCHAR(50) NULL");
                executeIgnoreError(stmt, "ALTER TABLE events MODIFY COLUMN location_address TEXT NULL");
                executeIgnoreError(stmt, "ALTER TABLE events MODIFY COLUMN location_detail TEXT NULL");
                executeIgnoreError(stmt, "ALTER TABLE events MODIFY COLUMN location_lat DOUBLE NULL");
                executeIgnoreError(stmt, "ALTER TABLE events MODIFY COLUMN location_lng DOUBLE NULL");
                // Schedule 테이블에서 제거된 time 컬럼을 nullable로 변경 (기존 DB 호환성)
                executeIgnoreError(stmt, "ALTER TABLE schedules MODIFY COLUMN time TIME NULL DEFAULT NULL");
                // account_groups.wedding_id 를 nullable로 변경 (migrate_host_account_to_event.sql 미적용 보정)
                executeIgnoreError(stmt, "ALTER TABLE account_groups MODIFY COLUMN wedding_id BIGINT NULL DEFAULT NULL");
                // schedules.wedding_id 를 nullable로 변경 (migrate_host_account_to_event.sql 미적용 보정)
                executeIgnoreError(stmt, "ALTER TABLE schedules MODIFY COLUMN wedding_id BIGINT NULL DEFAULT NULL");
                // galleries.order_index 를 nullable로 변경 (Gallery에서 order_index 제거됨)
                executeIgnoreError(stmt, "ALTER TABLE galleries MODIFY COLUMN order_index INT NULL DEFAULT NULL");
                // hosts.email 을 nullable로 변경 (GATHERING 호스트는 이메일 없을 수 있음)
                executeIgnoreError(stmt, "ALTER TABLE hosts MODIFY COLUMN email VARCHAR(255) NULL DEFAULT NULL");
                // hosts 테이블의 제거된 컬럼들을 nullable로 변경 (migrate_host_account_to_event.sql 미적용 보정)
                executeIgnoreError(stmt, "ALTER TABLE hosts MODIFY COLUMN father_name VARCHAR(50) NULL DEFAULT NULL");
                executeIgnoreError(stmt, "ALTER TABLE hosts MODIFY COLUMN mother_name VARCHAR(50) NULL DEFAULT NULL");
                executeIgnoreError(stmt, "ALTER TABLE hosts MODIFY COLUMN is_father_alive TINYINT(1) NULL DEFAULT NULL");
                executeIgnoreError(stmt, "ALTER TABLE hosts MODIFY COLUMN is_mother_alive TINYINT(1) NULL DEFAULT NULL");
            } catch (SQLException e) {
                // ignore top-level
            }
        }

        private void executeIgnoreError(Statement stmt, String sql) {
            try {
                stmt.execute(sql);
            } catch (SQLException e) {
                // 이미 nullable이거나 컬럼이 없는 경우 무시
            }
        }
    }
}
