package com.net.jianjia.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 在URL路径段中替换指定的参数值，使用该注解定义的参数的值不可为空。
 *
 * @author 裴云飞
 * @date 2021 /1/23
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Path {

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
