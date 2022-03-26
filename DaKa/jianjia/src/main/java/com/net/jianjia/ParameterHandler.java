package com.net.jianjia;

import okhttp3.Headers;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * The type Parameter handler.
 *
 * @param <T> the type parameter
 * @modify&fix 田梓萱
 * @date 2022 /2/17
 */
abstract class ParameterHandler<T> {

    /**
     * Apply.
     *
     * @param builder the builder
     * @param value   the value
     * @throws IOException the io exception
     */
    abstract void apply(RequestBuilder builder, T value) throws IOException;

    /**
     * Iterable parameter handler.
     *
     * @return the parameter handler
     */
    final ParameterHandler<Iterable<T>> iterable() {
        return new ParameterHandler<Iterable<T>>() {
            @Override
            void apply(RequestBuilder builder, Iterable<T> value) throws IOException {
                if (value == null) {
                    return;
                }
                for (T t : value) {
                    ParameterHandler.this.apply(builder, t);
                }
            }
        };
    }

    /**
     * Array parameter handler.
     *
     * @return the parameter handler
     */
    final ParameterHandler<Object> array() {
        return new ParameterHandler<Object>() {
            @Override
            void apply(RequestBuilder builder, Object value) throws IOException {
                if (value == null) {
                    return;
                }
                int size = Array.getLength(value);
                for (int i = 0; i < size; i++) {
                    //noinspection unchecked
                    ParameterHandler.this.apply(builder, (T) Array.get(value, i));
                }
            }
        };
    }

    /**
     * The type Relative url.
     */
    static final class RelativeUrl extends ParameterHandler<Object> {

        /**
         * The Method.
         */
        Method method;
        /**
         * The P.
         */
        int p;

        /**
         * Instantiates a new Relative url.
         *
         * @param method the method
         * @param p      the p
         */
        RelativeUrl(Method method, int p) {
            this.method = method;
            this.p = p;
        }

        /**
         * Apply.
         *
         * @param builder the builder
         * @param value   the value
         * @throws IOException the io exception
         */
        @Override
        void apply(RequestBuilder builder, Object value) throws IOException {
            if (value == null) {
                throw Utils.parameterError(method, p, "@Url parameter is null.");
            }
            builder.setRelativeUrl(value);
        }
    }

    /**
     * The type Path.
     *
     * @param <T> the type parameter
     */
    static final class Path<T> extends ParameterHandler<T> {

        /**
         * The Method.
         */
        Method method;
        /**
         * The P.
         */
        int p;
        /**
         * The Name.
         */
        String name;
        /**
         * The Value converter.
         */
        Converter<T, String> valueConverter;
        /**
         * The Encoded.
         */
        boolean encoded;

        /**
         * Instantiates a new Path.
         *
         * @param method         the method
         * @param p              the p
         * @param name           the name
         * @param valueConverter the value converter
         * @param encoded        the encoded
         */
        Path(Method method, int p, String name, Converter<T, String> valueConverter, boolean encoded) {
            this.method = method;
            this.p = p;
            this.name = name;
            this.valueConverter = valueConverter;
            this.encoded = encoded;
        }

        /**
         * Apply.
         *
         * @param builder the builder
         * @param value   the value
         * @throws IOException the io exception
         */
        @Override
        void apply(RequestBuilder builder, T value) throws IOException {
            if (value == null) {
                throw Utils.parameterError(method, p,
                        "Path parameter \"" + name + "\" value must not be null.");
            }
            builder.addPathParam(name, valueConverter.convert(value), encoded);
        }
    }

    /**
     * The type Query.
     *
     * @param <T> the type parameter
     */
    static final class Query<T> extends ParameterHandler<T> {

        /**
         * The Name.
         */
        private final String name;
        /**
         * The Value converter.
         */
        private final Converter<T, String> valueConverter;
        /**
         * The Encoded.
         */
        private final boolean encoded;

        /**
         * Instantiates a new Query.
         *
         * @param name           the name
         * @param valueConverter the value converter
         * @param encoded        the encoded
         */
        Query(String name, Converter<T, String> valueConverter, boolean encoded) {
            this.name = Utils.checkNotNull(name, "name == null");
            this.valueConverter = valueConverter;
            this.encoded = encoded;
        }

        /**
         * Apply.
         *
         * @param builder the builder
         * @param value   the value
         * @throws IOException the io exception
         */
        @Override
        void apply(RequestBuilder builder, T value) throws IOException {
            if (value == null) {
                return;
            }
            String queryValue = valueConverter.convert(value);
            if (queryValue == null) {
                return;
            }
            builder.addQueryParam(name, queryValue, encoded);
        }
    }

    /**
     * The type Query map.
     *
     * @param <T> the type parameter
     */
    static final class QueryMap<T> extends ParameterHandler<Map<String, T>> {

        /**
         * The Method.
         */
        private final Method method;
        /**
         * The P.
         */
        private final int p;
        /**
         * The Value converter.
         */
        private final Converter<T, String> valueConverter;
        /**
         * The Encoded.
         */
        private final boolean encoded;

        /**
         * Instantiates a new Query map.
         *
         * @param method         the method
         * @param p              the p
         * @param valueConverter the value converter
         * @param encoded        the encoded
         */
        QueryMap(Method method, int p, Converter<T, String> valueConverter, boolean encoded) {
            this.method = method;
            this.p = p;
            this.valueConverter = valueConverter;
            this.encoded = encoded;
        }

        /**
         * Apply.
         *
         * @param builder the builder
         * @param value   the value
         * @throws IOException the io exception
         */
        @Override
        void apply(RequestBuilder builder, Map<String, T> value) throws IOException {
            if (value == null) {
                throw Utils.parameterError(method, p, "Query map was null");
            }
            for (Map.Entry<String, T> entry : value.entrySet()) {
                String entryKey = entry.getKey();
                if (entryKey == null) {
                    throw Utils.parameterError(method, p, "Query map contained null key.");
                }
                T entryValue = entry.getValue();
                if (entryValue == null) {
                    throw Utils.parameterError(method, p,
                            "Query map contained null value for key '" + entryKey + "'.");
                }
                String convertedEntryValue = valueConverter.convert(entryValue);
                if (convertedEntryValue == null) {
                    throw Utils.parameterError(method, p, "Query map value '"
                            + entryValue
                            + "' converted to null by "
                            + valueConverter.getClass().getName()
                            + " for key '"
                            + entryKey
                            + "'.");
                }
                builder.addQueryParam(entryKey, convertedEntryValue, encoded);
            }
        }
    }

    /**
     * The type Header.
     *
     * @param <T> the type parameter
     */
    static final class Header<T> extends ParameterHandler<T> {

        /**
         * The Name.
         */
        private final String name;
        /**
         * The Value converter.
         */
        private final Converter<T, String> valueConverter;

        /**
         * Instantiates a new Header.
         *
         * @param name           the name
         * @param valueConverter the value converter
         */
        Header(String name, Converter<T, String> valueConverter) {
            this.name = Utils.checkNotNull(name, "name == null");
            this.valueConverter = valueConverter;
        }

        /**
         * Apply.
         *
         * @param builder the builder
         * @param value   the value
         * @throws IOException the io exception
         */
        @Override
        void apply(RequestBuilder builder, T value) throws IOException {
            if (value == null) {
                return;
            }
            String queryValue = valueConverter.convert(value);
            if (queryValue == null) {
                return;
            }
            builder.addHeader(name, queryValue);
        }
    }

    /**
     * The type Header map.
     *
     * @param <T> the type parameter
     */
    static final class HeaderMap<T> extends ParameterHandler<Map<String, T>> {

        /**
         * The Method.
         */
        private final Method method;
        /**
         * The P.
         */
        private final int p;
        /**
         * The Value converter.
         */
        private final Converter<T, String> valueConverter;

        /**
         * Instantiates a new Header map.
         *
         * @param method         the method
         * @param p              the p
         * @param valueConverter the value converter
         */
        HeaderMap(Method method, int p, Converter<T, String> valueConverter) {
            this.method = method;
            this.p = p;
            this.valueConverter = valueConverter;
        }

        /**
         * Apply.
         *
         * @param builder the builder
         * @param value   the value
         * @throws IOException the io exception
         */
        @Override
        void apply(RequestBuilder builder, Map<String, T> value) throws IOException {
            if (value == null) {
                throw Utils.parameterError(method, p, "Header map was null");
            }
            for (Map.Entry<String, T> entry : value.entrySet()) {
                String headerName = entry.getKey();
                if (headerName == null) {
                    throw Utils.parameterError(method, p, "Header map contained null key.");
                }
                T headerValue = entry.getValue();
                if (headerValue == null) {
                    throw Utils.parameterError(method, p,
                            "Header map contained null value for key '" + headerName + "'.");
                }
                builder.addHeader(headerName, valueConverter.convert(headerValue));
            }
        }
    }

    /**
     * The type Field.
     *
     * @param <T> the type parameter
     */
    static final class Field<T> extends ParameterHandler<T> {

        /**
         * The Name.
         */
        private final String name;
        /**
         * The Value converter.
         */
        private final Converter<T, String> valueConverter;
        /**
         * The Encoded.
         */
        private final boolean encoded;

        /**
         * Instantiates a new Field.
         *
         * @param name           the name
         * @param valueConverter the value converter
         * @param encoded        the encoded
         */
        Field(String name, Converter<T, String> valueConverter, boolean encoded) {
            this.name = Utils.checkNotNull(name, "name == null");
            this.valueConverter = valueConverter;
            this.encoded = encoded;
        }

        /**
         * Apply.
         *
         * @param builder the builder
         * @param value   the value
         * @throws IOException the io exception
         */
        @Override
        void apply(RequestBuilder builder, T value) throws IOException {
            if (value == null) {
                return;
            }
            String fieldValue = valueConverter.convert(value);
            if (fieldValue == null) {
                return;
            }
            builder.addFormField(name, fieldValue, encoded);
        }
    }

    /**
     * The type Field map.
     *
     * @param <T> the type parameter
     */
    static final class FieldMap<T> extends ParameterHandler<Map<String, T>> {

        /**
         * The Method.
         */
        private final Method method;
        /**
         * The P.
         */
        private final int p;
        /**
         * The Value converter.
         */
        private final Converter<T, String> valueConverter;
        /**
         * The Encoded.
         */
        private final boolean encoded;

        /**
         * Instantiates a new Field map.
         *
         * @param method         the method
         * @param p              the p
         * @param valueConverter the value converter
         * @param encoded        the encoded
         */
        FieldMap(Method method, int p, Converter<T, String> valueConverter, boolean encoded) {
            this.method = method;
            this.p = p;
            this.valueConverter = valueConverter;
            this.encoded = encoded;
        }

        /**
         * Apply.
         *
         * @param builder the builder
         * @param value   the value
         * @throws IOException the io exception
         */
        @Override
        void apply(RequestBuilder builder, Map<String, T> value) throws IOException {
            if (value == null) {
                throw Utils.parameterError(method, p, "Query map was null");
            }

            for (Map.Entry<String, T> entry : value.entrySet()) {
                String entryKey = entry.getKey();
                if (entryKey == null) {
                    throw Utils.parameterError(method, p, "Query map contained null key.");
                }
                T entryValue = entry.getValue();
                if (entryValue == null) {
                    throw Utils.parameterError(method, p,
                            "Query map contained null value for key '" + entryKey + "'.");
                }
                String fieldEntry = valueConverter.convert(entryValue);
                if (fieldEntry == null) {
                    throw Utils.parameterError(method, p, "Query map value '"
                            + entryValue
                            + "' converted to null by "
                            + valueConverter.getClass().getName()
                            + " for key '"
                            + entryKey
                            + "'.");
                }
                builder.addFormField(entryKey, fieldEntry, encoded);
            }
        }
    }

    /**
     * The type Body.
     *
     * @param <T> the type parameter
     */
    static final class Body<T> extends ParameterHandler<T> {

        /**
         * The Method.
         */
        final Method method;
        /**
         * The P.
         */
        final int p;
        /**
         * The Converter.
         */
        final Converter<T, RequestBody> converter;

        /**
         * Instantiates a new Body.
         *
         * @param method    the method
         * @param p         the p
         * @param converter the converter
         */
        Body(Method method, int p, Converter<T, RequestBody> converter) {
            this.method = method;
            this.p = p;
            this.converter = converter;
        }

        /**
         * Apply.
         *
         * @param builder the builder
         * @param value   the value
         * @throws IOException the io exception
         */
        @Override
        void apply(RequestBuilder builder, T value) throws IOException {
            if (value == null) {
                throw Utils.parameterError(method, p, "Body parameter value must not be null.");
            }
            RequestBody body;
            try {
                body = converter.convert(value);
            } catch (IOException e) {
                throw Utils.parameterError(method, e, p, "Unable to convert " + value + " to RequestBody");
            }
            builder.setBody(body);
        }
    }

    /**
     * The type Part.
     *
     * @param <T> the type parameter
     */
    static final class Part<T> extends ParameterHandler<T> {
        /**
         * The Method.
         */
        private final Method method;
        /**
         * The P.
         */
        private final int p;
        /**
         * The Headers.
         */
        private final Headers headers;
        /**
         * The Converter.
         */
        private final Converter<T, RequestBody> converter;

        /**
         * Instantiates a new Part.
         *
         * @param method    the method
         * @param p         the p
         * @param headers   the headers
         * @param converter the converter
         */
        Part(Method method, int p, Headers headers, Converter<T, RequestBody> converter) {
            this.method = method;
            this.p = p;
            this.headers = headers;
            this.converter = converter;
        }

        /**
         * Apply.
         *
         * @param builder the builder
         * @param value   the value
         */
        @Override
        void apply(RequestBuilder builder, T value) {
            if (value == null) return; // Skip null values.

            RequestBody body;
            try {
                body = converter.convert(value);
            } catch (IOException e) {
                throw Utils.parameterError(method, p, "Unable to convert " + value + " to RequestBody", e);
            }
            builder.addPart(headers, body);
        }
    }

    /**
     * The type Raw part.
     */
    static final class RawPart extends ParameterHandler<MultipartBody.Part> {
        /**
         * The Instance.
         */
        static final RawPart INSTANCE = new RawPart();

        /**
         * Instantiates a new Raw part.
         */
        private RawPart() {
        }

        /**
         * Apply.
         *
         * @param builder the builder
         * @param value   the value
         */
        @Override
        void apply(RequestBuilder builder, MultipartBody.Part value) {
            if (value != null) { // Skip null values.
                builder.addPart(value);
            }
        }
    }

    /**
     * The type Part map.
     *
     * @param <T> the type parameter
     */
    static final class PartMap<T> extends ParameterHandler<Map<String, T>> {
        /**
         * The Method.
         */
        private final Method method;
        /**
         * The P.
         */
        private final int p;
        /**
         * The Value converter.
         */
        private final Converter<T, RequestBody> valueConverter;
        /**
         * The Transfer encoding.
         */
        private final String transferEncoding;

        /**
         * Instantiates a new Part map.
         *
         * @param method           the method
         * @param p                the p
         * @param valueConverter   the value converter
         * @param transferEncoding the transfer encoding
         */
        PartMap(Method method, int p,
                Converter<T, RequestBody> valueConverter, String transferEncoding) {
            this.method = method;
            this.p = p;
            this.valueConverter = valueConverter;
            this.transferEncoding = transferEncoding;
        }

        /**
         * Apply.
         *
         * @param builder the builder
         * @param value   the value
         * @throws IOException the io exception
         */
        @Override
        void apply(RequestBuilder builder, Map<String, T> value)
                throws IOException {
            if (value == null) {
                throw Utils.parameterError(method, p, "Part map was null.");
            }

            for (Map.Entry<String, T> entry : value.entrySet()) {
                String entryKey = entry.getKey();
                if (entryKey == null) {
                    throw Utils.parameterError(method, p, "Part map contained null key.");
                }
                T entryValue = entry.getValue();
                if (entryValue == null) {
                    throw Utils.parameterError(method, p,
                            "Part map contained null value for key '" + entryKey + "'.");
                }

                Headers headers = Headers.of(
                        "Content-Disposition", "form-data; name=\"" + entryKey + "\"",
                        "Content-Transfer-Encoding", transferEncoding);

                builder.addPart(headers, valueConverter.convert(entryValue));
            }
        }
    }
}
