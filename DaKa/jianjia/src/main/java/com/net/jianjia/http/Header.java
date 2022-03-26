package com.net.jianjia.http;

import java.lang.annotation.*;

/**
 * 作用于参数上的注解，用于添加请求头
 *
 * @author 裴云飞
 * @date 2021 /1/25
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Header {

    /**
     * Value string.
     *
     * @return the string
     */
    String value();
}
