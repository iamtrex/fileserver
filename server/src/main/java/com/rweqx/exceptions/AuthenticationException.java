package com.rweqx.exceptions;

public class AuthenticationException extends Exception {

    private int code;
    private String message;

    public AuthenticationException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;

    }

    public AuthenticationException(String message) {
        super(message);
        this.code = -1;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
