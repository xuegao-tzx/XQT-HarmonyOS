package com.net.jianjia.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 在鸿蒙系统下，默认是将服务端的响应回调到主线程，
 * 如果在方法上使用该注解，那就不会将服务端的响应回调到主线程
 *
 * @author 裴云飞
 * @date 2021 /1/23
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SkipCallbackExecutor {

}
