package com.rweqx.exceptions;

import java.sql.SQLException;

public class DatabaseException extends ServerException {
    private SQLException exception;

    public DatabaseException(int code, String message) {
        super(code, message);
    }

    public DatabaseException(int code, String message, SQLException e) {
        super(code, message);
        exception = e;
    }
}
