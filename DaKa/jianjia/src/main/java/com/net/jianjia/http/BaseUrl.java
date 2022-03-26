package com.net.jianjia.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 动态更换接口域名，一个应用的域名可能有多个，如果接口的域名与默认指定的域名不相同，
 * 那就可以使用该注解，在方法上指定接口的域名
 *
 * @author 裴云飞
 * @date 2021 /1/24
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BaseUrl {

    /**
     * Value string.
     *
     * @return the string
     */
    String value();
}
