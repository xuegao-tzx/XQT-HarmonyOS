package com.net.jianjia.conventer;

import com.net.jianjia.Converter;
import okhttp3.MediaType;
import okhttp3.RequestBody;

import java.io.IOException;

/**
 * The type Scalar request body converter.
 *
 * @param <T> the type parameter
 */
final class ScalarRequestBodyConverter<T> implements Converter<T, RequestBody> {

    /**
     * The Instance.
     */
    static final ScalarRequestBodyConverter<Object> INSTANCE = new ScalarRequestBodyConverter<>();
    /**
     * The constant MEDIA_TYPE.
     */
    private static final MediaType MEDIA_TYPE = MediaType.get("text/plain; charset=UTF-8");

    /**
     * Instantiates a new Scalar request body converter.
     */
    private ScalarRequestBodyConverter() {
    }

    /**
     * Convert request body.
     *
     * @param value the value
     * @return the request body
     * @throws IOException the io exception
     */
    @Override
    public RequestBody convert(T value) throws IOException {
        return RequestBody.create(MEDIA_TYPE, String.valueOf(value));
    }
}