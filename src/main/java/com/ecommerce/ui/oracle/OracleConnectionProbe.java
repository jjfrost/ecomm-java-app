package com.ecommerce.ui.oracle;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class OracleConnectionProbe {
    private static final String CONNECTION_QUERY = """
            SELECT SYS_CONTEXT('USERENV', 'DB_NAME'),
                   SYS_CONTEXT('USERENV', 'SERVICE_NAME'),
                   SYS_CONTEXT('USERENV', 'SESSION_USER'),
                   'ECOMM',
                   banner
              FROM v$version
             WHERE banner LIKE 'Oracle Database%'
               AND ROWNUM = 1
            """;

    private final JdbcTemplate jdbcTemplate;

    public OracleConnectionProbe(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public OracleConnectionInfo currentConnection() {
        return jdbcTemplate.queryForObject(
                CONNECTION_QUERY,
                (resultSet, rowNumber) -> new OracleConnectionInfo(
                        resultSet.getString(1),
                        resultSet.getString(2),
                        resultSet.getString(3),
                        resultSet.getString(4),
                        resultSet.getString(5)));
    }
}
