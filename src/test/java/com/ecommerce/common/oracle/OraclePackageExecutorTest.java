package com.ecommerce.common.oracle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OraclePackageExecutorTest {
    private DataSource dataSource;
    private Connection connection;
    private CallableStatement statement;
    private OraclePackageExecutor executor;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(CallableStatement.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareCall("{call demo_pkg.run(?)}")).thenReturn(statement);
        when(connection.prepareCall("{? = call demo_pkg.value(?)}")).thenReturn(statement);
        executor = new OraclePackageExecutor(dataSource);
    }

    @Test
    void executesProcedureWithBoundArguments() throws SQLException {
        executor.execute(
                "demo_pkg.run",
                "{call demo_pkg.run(?)}",
                callable -> callable.setLong(1, 42L));

        verify(statement).setLong(1, 42L);
        verify(statement).execute();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void executesFunctionAndReturnsTypedValue() throws SQLException {
        when(statement.getObject(1, Long.class)).thenReturn(99L);

        Long result = executor.executeFunction(
                "demo_pkg.value",
                "{? = call demo_pkg.value(?)}",
                1,
                Types.NUMERIC,
                Long.class,
                callable -> callable.setLong(2, 7L));

        assertThat(result).isEqualTo(99L);
        verify(statement).registerOutParameter(1, Types.NUMERIC);
        verify(statement).setLong(2, 7L);
        verify(statement).execute();
    }

    @Test
    void wrapsSqlFailureWithRoutineContext() throws SQLException {
        when(statement.execute()).thenThrow(new SQLException("database unavailable"));

        assertThatThrownBy(() -> executor.execute(
                "demo_pkg.run",
                "{call demo_pkg.run(?)}",
                StatementBinder.NONE))
                .isInstanceOf(OracleRoutineException.class)
                .hasMessageContaining("demo_pkg.run")
                .hasCauseInstanceOf(SQLException.class);
    }
}
