package com.rweqx.exceptions;

/**
 * Thrown when user does not have authorization to complete certain actions
 * ex - accessing a shared file they have no access to / is expired.
 */
public class AuthorizationException extends ServerException {

    public AuthorizationException(int code, String message) {
        super(code, message);
    }
}
