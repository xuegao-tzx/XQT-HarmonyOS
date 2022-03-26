package com.net.jianjia.gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.net.jianjia.Converter;
import com.net.jianjia.JianJia;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * The type Gson converter factory.
 *
 * @author 裴云飞
 * @date 2021 /1/26
 */
public final class GsonConverterFactory extends Converter.Factory {

    /**
     * The Gson.
     */
    private final Gson gson;

    /**
     * Instantiates a new Gson converter factory.
     *
     * @param gson the gson
     */
    private GsonConverterFactory(Gson gson) {
        this.gson = gson;
    }

    /**
     * Create gson converter factory.
     *
     * @return the gson converter factory
     */
    public static GsonConverterFactory create() {
        return create(new Gson());
    }

    /**
     * Create gson converter factory.
     *
     * @param gson the gson
     * @return the gson converter factory
     */
    public static GsonConverterFactory create(Gson gson) {
        if (gson == null) throw new NullPointerException("gson == null");
        return new GsonConverterFactory(gson);
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
    public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations, JianJia jianJia) {
        TypeAdapter<?> adapter = this.gson.getAdapter(TypeToken.get(type));
        return new GsonRequestBodyConverter<>(this.gson, adapter);
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
        TypeAdapter<?> adapter = this.gson.getAdapter(TypeToken.get(type));
        return new GsonResponseBodyConverter<>(this.gson, adapter);
    }
}
