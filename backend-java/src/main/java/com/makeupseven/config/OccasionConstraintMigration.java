package com.makeupseven.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Hibernate ddl-auto does not update Postgres CHECK constraints when enums grow.
 * Ensures HALDI_MEHENDI and GLAMOROUS are allowed before seeding portfolio/services.
 */
@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class OccasionConstraintMigration implements CommandLineRunner {

    private static final String OCCASIONS = "'WEDDING','BRIDAL','PARTY','GLAMOROUS','HALDI_MEHENDI',"
            + "'EDITORIAL','FILM','PERSONAL_EVENT','ENGAGEMENT','RECEPTION'";

    private final JdbcTemplate jdbc;

    @Override
    public void run(String... args) {
        updateConstraint("bookings", false);
        updateConstraint("quote_requests", false);
        updateConstraint("portfolio_items", false);
        updateConstraint("mua_services", true);
        updateConstraint("mua_occasions", false);
    }

    private void updateConstraint(String table, boolean nullable) {
        try {
            jdbc.execute("ALTER TABLE " + table + " DROP CONSTRAINT IF EXISTS " + table + "_occasion_check");
            String check = nullable
                    ? "CHECK (occasion IS NULL OR occasion::text = ANY (ARRAY[" + OCCASIONS + "]::text[]))"
                    : "CHECK (occasion::text = ANY (ARRAY[" + OCCASIONS + "]::text[]))";
            jdbc.execute("ALTER TABLE " + table + " ADD CONSTRAINT " + table + "_occasion_check " + check);
            log.debug("Updated occasion check constraint on {}", table);
        } catch (Exception e) {
            log.debug("Skipping occasion constraint on {} (table may not exist yet): {}", table, e.getMessage());
        }
    }
}
