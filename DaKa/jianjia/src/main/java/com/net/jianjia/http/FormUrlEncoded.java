package com.net.jianjia.http;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 用于发送一个表单请求，使用该注解必须在方法的参数添加{@link Field @Field}注解
 *
 * @author 裴云飞
 * @date 2021 /1/18
 */
@Documented
@Target(METHOD)
@Retention(RUNTIME)
public @interface FormUrlEncoded {
}
