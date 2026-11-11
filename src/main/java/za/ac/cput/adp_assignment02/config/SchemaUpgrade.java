package za.ac.cput.adp_assignment02.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/*
 * SchemaUpgrade.java
 * Hibernate (ddl-auto=update) creates enum columns as MySQL ENUMs and never changes them again.
 * Older databases therefore reject newer notification types (stock alerts, password resets)
 * with a 500 error. On start-up this turns those columns into plain text columns, once.
 * Safe to run every time: if the column is already text (or the database isn't MySQL) nothing happens.
 */
@Component
@Order(0)
public class SchemaUpgrade implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SchemaUpgrade.class);
    private final JdbcTemplate jdbc;

    public SchemaUpgrade(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(ApplicationArguments args) {
        widen("notification", "type");
        widen("stock_alert", "status");
        widen("orders", "payment_method");
    }

    private void widen(String table, String column) {
        try {
            String dataType = jdbc.queryForObject(
                    "select data_type from information_schema.columns where table_schema = database() and table_name = ? and column_name = ?",
                    String.class, table, column);
            if ("enum".equalsIgnoreCase(dataType)) {
                jdbc.execute("alter table " + table + " modify " + column + " varchar(40) not null");
                log.info("Upgraded {}.{} from ENUM to VARCHAR so new values can be stored.", table, column);
            }
        } catch (Exception e) {
            // table not created yet, or not a MySQL database (e.g. the H2 test database) – nothing to do
            log.debug("Schema upgrade skipped for {}.{}: {}", table, column, e.getMessage());
        }
    }
}
