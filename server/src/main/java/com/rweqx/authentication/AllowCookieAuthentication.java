package com.rweqx.authentication;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Allows method to be validated using cookies
 */
@Retention(RUNTIME)
@Target({METHOD})
public @interface AllowCookieAuthentication {
}
