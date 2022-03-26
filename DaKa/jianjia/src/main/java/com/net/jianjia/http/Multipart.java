package com.net.jianjia.http;

import java.lang.annotation.*;

/**
 * 表示请求体是多部分的，每一部分作为一个参数，且用{@link Part @Part}注解声明
 *
 * @author 裴云飞
 * @date 2021 /1/18
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Multipart {

    /**
     * PUT注解一般必须添加相对路径或绝对路径或者全路径，如果不想在PUT注解后添加请求路径，
     * 则可以在方法的第一个参数中用{@link com.net.jianjia.http.Url @Url}注解添加请求路径
     *
     * @return string string
     */
    String value() default "";
}
