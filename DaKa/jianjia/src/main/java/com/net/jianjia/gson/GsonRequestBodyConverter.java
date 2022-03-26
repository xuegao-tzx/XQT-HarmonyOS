package com.net.jianjia.gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonWriter;
import com.net.jianjia.Converter;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * The type Gson request body converter.
 *
 * @param <T> the type parameter
 * @author 裴云飞
 * @date 2021 /1/26
 */
final class GsonRequestBodyConverter<T> implements Converter<T, RequestBody> {

    /**
     * The constant MEDIA_TYPE.
     */
    private static final MediaType MEDIA_TYPE = MediaType.get("application/json; charset=UTF-8");
    /**
     * The constant UTF_8.
     */
    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    /**
     * The Gson.
     */
    private final Gson gson;
    /**
     * The Adapter.
     */
    private final TypeAdapter<T> adapter;

    /**
     * Instantiates a new Gson request body converter.
     *
     * @param gson    the gson
     * @param adapter the adapter
     */
    GsonRequestBodyConverter(Gson gson, TypeAdapter<T> adapter) {
        this.gson = gson;
        this.adapter = adapter;
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
        Buffer buffer = new Buffer();
        Writer writer = new OutputStreamWriter(buffer.outputStream(), UTF_8);
        JsonWriter jsonWriter = this.gson.newJsonWriter(writer);
        this.adapter.write(jsonWriter, value);
        jsonWriter.close();
        return RequestBody.create(MEDIA_TYPE, buffer.readByteString());
    }
}
