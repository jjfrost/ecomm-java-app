package com.ecommerce.common.oracle;

public class OracleRoutineException extends RuntimeException {
    private final String routineName;

    public OracleRoutineException(String routineName, Throwable cause) {
        super("Oracle routine failed: " + routineName, cause);
        this.routineName = routineName;
    }

    public String getRoutineName() {
        return routineName;
    }
}
