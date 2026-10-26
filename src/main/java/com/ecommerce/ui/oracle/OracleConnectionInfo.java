package com.ecommerce.ui.oracle;

public record OracleConnectionInfo(
        String databaseName,
        String serviceName,
        String sessionUser,
        String applicationSchema,
        String version) {
}
