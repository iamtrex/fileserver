package com.rweqx.exceptions;

public class ServerException extends RuntimeException {

    private int code;
    private String message;

    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public ServerException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;

    }
}
