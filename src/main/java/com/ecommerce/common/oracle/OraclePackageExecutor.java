package com.ecommerce.common.oracle;

import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class OraclePackageExecutor {
    private final DataSource dataSource;

    public OraclePackageExecutor(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(
            String routineName,
            String callSyntax,
            StatementBinder binder) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (CallableStatement statement = connection.prepareCall(callSyntax)) {
            binder.bind(statement);
            statement.execute();
        } catch (SQLException exception) {
            throw new OracleRoutineException(routineName, exception);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    public <T> T executeFunction(
            String routineName,
            String callSyntax,
            int outputIndex,
            int jdbcType,
            Class<T> outputType,
            StatementBinder binder) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (CallableStatement statement = connection.prepareCall(callSyntax)) {
            statement.registerOutParameter(outputIndex, jdbcType);
            binder.bind(statement);
            statement.execute();
            return statement.getObject(outputIndex, outputType);
        } catch (SQLException exception) {
            throw new OracleRoutineException(routineName, exception);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }
}
