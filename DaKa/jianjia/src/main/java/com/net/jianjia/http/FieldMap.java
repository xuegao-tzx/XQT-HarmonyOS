package com.net.jianjia.http;

import java.lang.annotation.*;

/**
 * 以map的形式发送一个表单请求
 *
 * @author 裴云飞
 * @date 2021 /1/25
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldMap {

    /**
     * Encoded boolean.
     *
     * @return the boolean
     */
    boolean encoded() default false;
}
