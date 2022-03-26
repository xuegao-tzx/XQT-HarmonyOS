package com.net.jianjia.http;

import java.lang.annotation.*;

/**
 * 用于给get请求添加请求参数。使用该注解定义的参数，参数值可以为空，为空时，忽略该值。
 * 当传入一个集合或数组时，拼接请求键值对，所有的键是统一的，
 * 如: name=张三&name=李四&name=王五。
 *
 * @author 裴云飞
 * @date 2021 /1/24
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Query {

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
