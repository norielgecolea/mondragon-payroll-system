package com.mondragon.payroll.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Ensures newer payroll_items columns exist even when Hibernate ddl-auto
 * did not alter an existing table (common with running/partial restarts).
 */
@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class SchemaMigrator implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        String[] statements = {
                "ALTER TABLE payroll_items ADD COLUMN IF NOT EXISTS sss_deduction NUMERIC(12,2) NOT NULL DEFAULT 0",
                "ALTER TABLE payroll_items ADD COLUMN IF NOT EXISTS philhealth_deduction NUMERIC(12,2) NOT NULL DEFAULT 0",
                "ALTER TABLE payroll_items ADD COLUMN IF NOT EXISTS bonus NUMERIC(12,2) NOT NULL DEFAULT 0",
                "ALTER TABLE payroll_items ADD COLUMN IF NOT EXISTS deduct_sss BOOLEAN NOT NULL DEFAULT FALSE",
                "ALTER TABLE payroll_items ADD COLUMN IF NOT EXISTS deduct_philhealth BOOLEAN NOT NULL DEFAULT FALSE",
                "ALTER TABLE payroll_items ADD COLUMN IF NOT EXISTS daily_rate NUMERIC(12,2) NOT NULL DEFAULT 0",
                "ALTER TABLE payroll_items ADD COLUMN IF NOT EXISTS overtime_rate NUMERIC(12,2) NOT NULL DEFAULT 0",
                "ALTER TABLE payroll_items ADD COLUMN IF NOT EXISTS cash_advance_balance NUMERIC(12,2) NOT NULL DEFAULT 0"
        };
        for (String sql : statements) {
            jdbcTemplate.execute(sql);
        }
        log.info("Ensured payroll_items deduction/bonus columns exist");
    }
}
