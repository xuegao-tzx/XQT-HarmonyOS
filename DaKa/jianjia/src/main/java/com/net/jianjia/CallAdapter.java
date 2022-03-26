package com.net.jianjia;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;


/**
 * 将响应类型为{@code R}的{@link Call}适配为{@code T}类型。实例由对应的Factory来创建，
 * 这个对应的Factory是通过{@linkplain JianJia.Builder#addCallAdapterFactory(Factory)}方法添加到JianJia对象中的
 *
 * @param <R> the type parameter
 * @param <T> the type parameter
 * @modify&fix 田梓萱
 * @date 2022 /2/17
 */
public interface CallAdapter<R, T> {

    /**
     * 返回响应类型，例如，"Call <Repo>"的响应类型是"Repo"。
     *
     * @return type type
     */
    Type responseType();

    /**
     * Call<T> 是适配成另外一个另外一个对象
     *
     * @param call the call
     * @return t t
     */
    T adapt(Call<R> call);

    /**
     * The type Factory.
     */
    abstract class Factory {

        /**
         * 创建CallAdapter对象
         *
         * @param returnType  带有泛型的返回值
         * @param annotations the annotations
         * @param jianJia     the jian jia
         * @return call adapter
         */
        public abstract CallAdapter<?, ?> get(Type returnType, Annotation[] annotations,
                                              JianJia jianJia);
    }
}
