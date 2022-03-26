package com.net.jianjia.http;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 用于发送一个表单请求
 *
 * @author 裴云飞
 * @date 2021 /1/18
 */
@Documented
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface Field {

    /**
     * Value string.
     *
     * @return the string
     */
    String value();

    /**
     * Encoded boolean.
     *
     * @return the boolean
     */
    boolean encoded() default false;
}
