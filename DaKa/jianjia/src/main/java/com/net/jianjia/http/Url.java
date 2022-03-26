package com.net.jianjia.http;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 用于添加请求的接口地址
 *
 * @author 裴云飞
 * @date 2021 /1/23
 */
@Documented
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface Url {
}