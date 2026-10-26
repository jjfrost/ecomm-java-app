package com.ecommerce.common.oracle;

import java.sql.CallableStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface StatementBinder {
    StatementBinder NONE = statement -> {
    };

    void bind(CallableStatement statement) throws SQLException;
}
