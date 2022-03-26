package com.net.jianjia;

import com.net.jianjia.http.Streaming;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * 默认的数据转换器，如果调用者没有添加{@link Converter}对象，就会使用这个默认的数据转换器
 *
 * @modify&fix 田梓萱
 * @date 2022 /2/17
 */
class BuiltInConverters extends Converter.Factory {

    /**
     * Request body converter converter.
     *
     * @param type                 the type
     * @param parameterAnnotations the parameter annotations
     * @param methodAnnotations    the method annotations
     * @param jianJia              the jian jia
     * @return the converter
     */
    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type,
                                                          Annotation[] parameterAnnotations, Annotation[] methodAnnotations, JianJia jianJia) {
        if (RequestBody.class.isAssignableFrom(Utils.getRawType(type))) {
            return RequestBodyConverter.INSTANCE;
        }
        return null;
    }

    /**
     * Response body converter converter.
     *
     * @param type        the type
     * @param annotations the annotations
     * @param jianJia     the jian jia
     * @return the converter
     */
    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, JianJia jianJia) {
        if (type == ResponseBody.class) {
            return Utils.isAnnotationPresent(annotations, Streaming.class) ?
                    StreamingResponseBodyConverter.INSTANCE :
                    BufferingResponseBodyConverter.INSTANCE;
        }
        if (type == Void.class) {
            return VoidResponseBodyConverter.INSTANCE;
        }
        return null;
    }

    /**
     * The type Request body converter.
     */
    static final class RequestBodyConverter implements Converter<RequestBody, RequestBody> {
        /**
         * The Instance.
         */
        static final RequestBodyConverter INSTANCE = new RequestBodyConverter();

        /**
         * Convert request body.
         *
         * @param value the value
         * @return the request body
         */
        @Override
        public RequestBody convert(RequestBody value) {
            return value;
        }
    }

    /**
     * The type Streaming response body converter.
     */
    static final class StreamingResponseBodyConverter implements Converter<ResponseBody, ResponseBody> {

        /**
         * The Instance.
         */
        static final StreamingResponseBodyConverter INSTANCE = new StreamingResponseBodyConverter();

        /**
         * Convert response body.
         *
         * @param value the value
         * @return the response body
         * @throws IOException the io exception
         */
        @Override
        public ResponseBody convert(ResponseBody value) throws IOException {
            return value;
        }
    }

    /**
     * The type Buffering response body converter.
     */
    static final class BufferingResponseBodyConverter implements Converter<ResponseBody, ResponseBody> {

        /**
         * The Instance.
         */
        static final BufferingResponseBodyConverter INSTANCE = new BufferingResponseBodyConverter();

        /**
         * Convert response body.
         *
         * @param value the value
         * @return the response body
         * @throws IOException the io exception
         */
        @Override
        public ResponseBody convert(ResponseBody value) throws IOException {
            try {
                return Utils.buffer(value);
            } finally {
                value.close();
            }
        }
    }

    /**
     * The type Void response body converter.
     */
    static final class VoidResponseBodyConverter implements Converter<ResponseBody, Void> {

        /**
         * The Instance.
         */
        static final VoidResponseBodyConverter INSTANCE = new VoidResponseBodyConverter();

        /**
         * Convert void.
         *
         * @param value the value
         * @return the void
         * @throws IOException the io exception
         */
        @Override
        public Void convert(ResponseBody value) throws IOException {
            value.close();
            return null;
        }
    }

    /**
     * The type To string converter.
     */
    static final class ToStringConverter implements Converter<Object, String> {

        /**
         * The Instance.
         */
        static final ToStringConverter INSTANCE = new ToStringConverter();

        /**
         * 直接调用toString方法
         *
         * @param value the value
         * @return string string
         * @throws IOException the io exception
         */
        @Override
        public String convert(Object value) throws IOException {
            return value.toString();
        }
    }
}
