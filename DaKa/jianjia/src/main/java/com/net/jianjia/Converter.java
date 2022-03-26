package com.net.jianjia;

import com.net.jianjia.http.*;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * 数据转换器，Converter这个类的实例由Factory创建，由{@linkplain JianJia.Builder#addConverterFactory(Factory)}方法来进行初始化的
 *
 * @param <F> the type parameter
 * @param <T> the type parameter
 * @modify&fix 田梓萱
 * @date 2022 /2/17
 */
public interface Converter<F, T> {

    /**
     * 把F转化为T
     *
     * @param value the value
     * @return t t
     * @throws IOException the io exception
     */
    T convert(F value) throws IOException;

    /**
     * The type Factory.
     */
    abstract class Factory {

        /**
         * 返回一个处理请求体的转换器，这个转换器主要是为了处理{@link Body}注解，{@link Part}注解。
         *
         * @param type                 the type
         * @param parameterAnnotations the parameter annotations
         * @param methodAnnotations    the method annotations
         * @param jianJia              the jian jia
         * @return converter converter
         */
        public Converter<?, RequestBody> requestBodyConverter(Type type,
                                                              Annotation[] parameterAnnotations, Annotation[] methodAnnotations, JianJia jianJia) {
            return null;
        }

        /**
         * 返回一个处理响应体的转换器，例如：Call<SimpleResponse>，则响应体的类型应该是SimpleResponse。
         *
         * @param type        the type
         * @param annotations the annotations
         * @param jianJia     the jian jia
         * @return converter converter
         */
        public Converter<ResponseBody, ?> responseBodyConverter(Type type,
                                                                Annotation[] annotations, JianJia jianJia) {
            return null;
        }

        /**
         * 返回一个处理字符串的转换器，这个转换器主要是为了处理{@link Field}注解, {@link FieldMap}注解，
         * {@link Header}注解，{@link HeaderMap}注解，{@link Path}注解，{@link Query}注解，
         * {@link QueryMap}注解
         *
         * @param type        the type
         * @param annotations the annotations
         * @param jianJia     the jian jia
         * @return converter converter
         */
        Converter<?, String> stringConverter(Type type, Annotation[] annotations, JianJia jianJia) {
            return null;
        }
    }
}
