package com.net.jianjia.conventer;

import com.net.jianjia.Converter;
import com.net.jianjia.JianJia;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * A {@linkplain Converter.Factory converter} for strings and both primitives and their boxed types
 * to {@code text/plain} bodies.
 */
public final class ScalarsConverterFactory extends Converter.Factory {
    /**
     * Instantiates a new Scalars converter factory.
     */
    private ScalarsConverterFactory() {
    }

    /**
     * Create scalars converter factory.
     *
     * @return the scalars converter factory
     */
    public static ScalarsConverterFactory create() {
        return new ScalarsConverterFactory();
    }

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
        if (type == String.class
                || type == boolean.class
                || type == Boolean.class
                || type == byte.class
                || type == Byte.class
                || type == char.class
                || type == Character.class
                || type == double.class
                || type == Double.class
                || type == float.class
                || type == Float.class
                || type == int.class
                || type == Integer.class
                || type == long.class
                || type == Long.class
                || type == short.class
                || type == Short.class) {
            return ScalarRequestBodyConverter.INSTANCE;
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
    public Converter<ResponseBody, ?> responseBodyConverter(
            Type type, Annotation[] annotations, JianJia jianJia) {
        if (type == String.class) {
            return ScalarResponseBodyConverters.StringResponseBodyConverter.INSTANCE;
        }
        if (type == Boolean.class || type == boolean.class) {
            return ScalarResponseBodyConverters.BooleanResponseBodyConverter.INSTANCE;
        }
        if (type == Byte.class || type == byte.class) {
            return ScalarResponseBodyConverters.ByteResponseBodyConverter.INSTANCE;
        }
        if (type == Character.class || type == char.class) {
            return ScalarResponseBodyConverters.CharacterResponseBodyConverter.INSTANCE;
        }
        if (type == Double.class || type == double.class) {
            return ScalarResponseBodyConverters.DoubleResponseBodyConverter.INSTANCE;
        }
        if (type == Float.class || type == float.class) {
            return ScalarResponseBodyConverters.FloatResponseBodyConverter.INSTANCE;
        }
        if (type == Integer.class || type == int.class) {
            return ScalarResponseBodyConverters.IntegerResponseBodyConverter.INSTANCE;
        }
        if (type == Long.class || type == long.class) {
            return ScalarResponseBodyConverters.LongResponseBodyConverter.INSTANCE;
        }
        if (type == Short.class || type == short.class) {
            return ScalarResponseBodyConverters.ShortResponseBodyConverter.INSTANCE;
        }
        return null;
    }
}
