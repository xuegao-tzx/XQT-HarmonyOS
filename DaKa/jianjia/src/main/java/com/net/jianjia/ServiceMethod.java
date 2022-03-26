package com.net.jianjia;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * The type Service method.
 *
 * @param <T> the type parameter
 * @modify&fix 田梓萱
 * @date 2022 /2/17
 */
abstract class ServiceMethod<T> {

    /**
     * 解析注解
     *
     * @param <T>     the type parameter
     * @param jianJia the jian jia
     * @param method  the method
     * @return service method
     */
    static <T> ServiceMethod<T> parseAnnotations(JianJia jianJia, Method method) {
        RequestFactory requestFactory = RequestFactory.parseAnnotations(jianJia, method);
        Type returnType = method.getGenericReturnType();
        if (Utils.hasUnresolvableType(returnType)) {
            throw Utils.methodError(method,
                    "Method return type must not include a type variable or wildcard: %s", returnType);
        }
        if (returnType == void.class) {
            throw Utils.methodError(method, "Service methods cannot return void.");
        }
        return HttpServiceMethod.parseAnnotations(jianJia, method, requestFactory);
    }

    /**
     * Invoke t.
     *
     * @param args the args
     * @return the t
     */
    public abstract T invoke(Object[] args);
}
