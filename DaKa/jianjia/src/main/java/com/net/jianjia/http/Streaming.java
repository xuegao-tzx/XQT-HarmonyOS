package com.net.jianjia.http;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The interface Streaming.
 */
@Documented
@Target(METHOD)
@Retention(RUNTIME)
public @interface Streaming {
}