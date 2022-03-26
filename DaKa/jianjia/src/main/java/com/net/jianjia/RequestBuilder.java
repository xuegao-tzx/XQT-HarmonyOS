package com.net.jianjia;

import okhttp3.*;
import okio.Buffer;
import okio.BufferedSink;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * The type Request builder.
 *
 * @modify&fix 田梓萱
 * @date 2022 /2/17
 */
class RequestBuilder {

    /**
     * The constant HEX_DIGITS.
     */
    private static final char[] HEX_DIGITS =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    /**
     * The constant PATH_SEGMENT_ALWAYS_ENCODE_SET.
     */
    private static final String PATH_SEGMENT_ALWAYS_ENCODE_SET = " \"<>^`{}|\\?#";
    /**
     * The constant PATH_TRAVERSAL.
     */
    private static final Pattern PATH_TRAVERSAL = Pattern.compile("(.*/)?(\\.|%2e|%2E){1,2}(/.*)?");

    /**
     * The Method.
     */
    private final String method;

    /**
     * The Base url.
     */
    private final HttpUrl baseUrl;
    /**
     * The New base url.
     */
    private final HttpUrl newBaseUrl;
    /**
     * The Request builder.
     */
    private final Request.Builder requestBuilder;
    /**
     * The Has body.
     */
    private final boolean hasBody;
    /**
     * The Relative url.
     */
    private String relativeUrl;
    /**
     * The Url builder.
     */
    private HttpUrl.Builder urlBuilder;
    /**
     * The Content type.
     */
    private MediaType contentType;
    /**
     * The Multipart builder.
     */
    private MultipartBody.Builder multipartBuilder;
    /**
     * The Form builder.
     */
    private FormBody.Builder formBuilder;
    /**
     * The Body.
     */
    private RequestBody body;

    /**
     * Instantiates a new Request builder.
     *
     * @param method        the method
     * @param baseUrl       the base url
     * @param newBaseUrl    the new base url
     * @param relativeUrl   the relative url
     * @param headers       the headers
     * @param contentType   the content type
     * @param hasBody       the has body
     * @param isFormEncoded the is form encoded
     * @param isMultipart   the is multipart
     */
    RequestBuilder(String method, HttpUrl baseUrl, HttpUrl newBaseUrl, String relativeUrl, Headers headers, MediaType contentType,
                   boolean hasBody, boolean isFormEncoded, boolean isMultipart) {
        this.method = method;
        this.baseUrl = baseUrl;
        this.newBaseUrl = newBaseUrl;
        this.relativeUrl = relativeUrl;
        this.requestBuilder = new Request.Builder();
        this.contentType = contentType;
        this.hasBody = hasBody;

        if (headers != null) {
            this.requestBuilder.headers(headers);
        }
        if (isFormEncoded) {
            formBuilder = new FormBody.Builder();
        } else if (isMultipart) {
            multipartBuilder = new MultipartBody.Builder();
            multipartBuilder.setType(MultipartBody.FORM);
        }
    }

    /**
     * Canonicalize for path string.
     *
     * @param input          the input
     * @param alreadyEncoded the already encoded
     * @return the string
     */
    private static String canonicalizeForPath(String input, boolean alreadyEncoded) {
        int codePoint;
        for (int i = 0, limit = input.length(); i < limit; i += Character.charCount(codePoint)) {
            codePoint = input.codePointAt(i);
            if (codePoint < 0x20 || codePoint >= 0x7f
                    || PATH_SEGMENT_ALWAYS_ENCODE_SET.indexOf(codePoint) != -1
                    || (!alreadyEncoded && (codePoint == '/' || codePoint == '%'))) {
                // Slow path: the character at i requires encoding!
                Buffer out = new Buffer();
                out.writeUtf8(input, 0, i);
                canonicalizeForPath(out, input, i, limit, alreadyEncoded);
                return out.readUtf8();
            }
        }

        // Fast path: no characters required encoding.
        return input;
    }

    /**
     * Canonicalize for path.
     *
     * @param out            the out
     * @param input          the input
     * @param pos            the pos
     * @param limit          the limit
     * @param alreadyEncoded the already encoded
     */
    private static void canonicalizeForPath(Buffer out, String input, int pos, int limit,
                                            boolean alreadyEncoded) {
        Buffer utf8Buffer = null; // Lazily allocated.
        int codePoint;
        for (int i = pos; i < limit; i += Character.charCount(codePoint)) {
            codePoint = input.codePointAt(i);
            if (alreadyEncoded
                    && (codePoint == '\t' || codePoint == '\n' || codePoint == '\f' || codePoint == '\r')) {
                // Skip this character.
            } else if (codePoint < 0x20 || codePoint >= 0x7f
                    || PATH_SEGMENT_ALWAYS_ENCODE_SET.indexOf(codePoint) != -1
                    || (!alreadyEncoded && (codePoint == '/' || codePoint == '%'))) {
                // Percent encode this character.
                if (utf8Buffer == null) {
                    utf8Buffer = new Buffer();
                }
                utf8Buffer.writeUtf8CodePoint(codePoint);
                while (!utf8Buffer.exhausted()) {
                    int b = utf8Buffer.readByte() & 0xff;
                    out.writeByte('%');
                    out.writeByte(HEX_DIGITS[(b >> 4) & 0xf]);
                    out.writeByte(HEX_DIGITS[b & 0xf]);
                }
            } else {
                // This character doesn't need encoding. Just copy it over.
                out.writeUtf8CodePoint(codePoint);
            }
        }
    }

    /**
     * Get request . builder.
     *
     * @return the request . builder
     */
    Request.Builder get() {
        HttpUrl url;
        HttpUrl.Builder urlBuilder = this.urlBuilder;
        if (urlBuilder != null) {
            url = urlBuilder.build();
        } else {
            if (newBaseUrl != null) {
                url = newBaseUrl.resolve(relativeUrl);
            } else {
                url = baseUrl.resolve(relativeUrl);
            }
            if (url == null) {
                throw new IllegalArgumentException(
                        "Malformed URL. Base: " + baseUrl + ", Relative: " + relativeUrl);
            }
        }
        RequestBody body = this.body;
        if (body == null) {
            if (formBuilder != null) {
                body = formBuilder.build();
            } else if (multipartBuilder != null) {
                body = multipartBuilder.build();
            } else if (hasBody) {
                body = RequestBody.create(null, new byte[0]);
            }
        }
        MediaType contentType = this.contentType;
        if (contentType != null) {
            if (body != null) {
                body = new ContentTypeOverridingRequestBody(body, contentType);
            } else {
                requestBuilder.addHeader("Content-Type", contentType.toString());
            }
        }
        return requestBuilder.url(url).method(method, body);
    }

    /**
     * Sets body.
     *
     * @param body the body
     */
    void setBody(RequestBody body) {
        this.body = body;
    }

    /**
     * Add part.
     *
     * @param headers the headers
     * @param body    the body
     */
    void addPart(Headers headers, RequestBody body) {
        multipartBuilder.addPart(headers, body);
    }

    /**
     * Add part.
     *
     * @param part the part
     */
    @SuppressWarnings("ConstantConditions")
    // Only called when isMultipart was true.
    void addPart(MultipartBody.Part part) {
        multipartBuilder.addPart(part);
    }

    /**
     * Add form field.
     *
     * @param name    the name
     * @param value   the value
     * @param encoded the encoded
     */
    void addFormField(String name, String value, boolean encoded) {
        if (encoded) {
            formBuilder.addEncoded(name, value);
        } else {
            formBuilder.add(name, value);
        }
    }

    /**
     * Add path param.
     *
     * @param name    the name
     * @param value   the value
     * @param encoded the encoded
     */
    void addPathParam(String name, String value, boolean encoded) {
        if (relativeUrl == null) {
            throw new AssertionError();
        }
        String replacement = canonicalizeForPath(value, encoded);
        String newRelativeUrl = relativeUrl.replace("{" + name + "}", replacement);
        if (PATH_TRAVERSAL.matcher(newRelativeUrl).matches()) {
            throw new IllegalArgumentException(
                    "@Path parameters shouldn't perform path traversal ('.' or '..'): " + value);
        }
        relativeUrl = newRelativeUrl;
    }

    /**
     * Add query param.
     *
     * @param name    the name
     * @param value   the value
     * @param encoded the encoded
     */
    void addQueryParam(String name, String value, boolean encoded) {
        if (relativeUrl != null) {
            if (newBaseUrl != null) {
                urlBuilder = newBaseUrl.newBuilder(relativeUrl);
            } else {
                urlBuilder = baseUrl.newBuilder(relativeUrl);
            }
            if (urlBuilder == null) {
                throw new IllegalArgumentException(
                        "Malformed URL. Base: " + baseUrl + ", Relative: " + relativeUrl);
            }
            relativeUrl = null;
        }

        if (encoded) {
            urlBuilder.addEncodedQueryParameter(name, value);
        } else {
            urlBuilder.addQueryParameter(name, value);
        }
    }

    /**
     * Add header.
     *
     * @param name  the name
     * @param value the value
     */
    void addHeader(String name, String value) {
        if ("Content-Type".equalsIgnoreCase(name)) {
            try {
                contentType = MediaType.get(value);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Malformed content type: " + value, e);
            }
        } else {
            requestBuilder.addHeader(name, value);
        }
    }

    /**
     * Sets relative url.
     *
     * @param relativeUrl the relative url
     */
    void setRelativeUrl(Object relativeUrl) {
        this.relativeUrl = relativeUrl.toString();
    }

    /**
     * The type Content type overriding request body.
     */
    private static class ContentTypeOverridingRequestBody extends RequestBody {
        /**
         * The Delegate.
         */
        private final RequestBody delegate;
        /**
         * The Content type.
         */
        private final MediaType contentType;

        /**
         * Instantiates a new Content type overriding request body.
         *
         * @param delegate    the delegate
         * @param contentType the content type
         */
        ContentTypeOverridingRequestBody(RequestBody delegate, MediaType contentType) {
            this.delegate = delegate;
            this.contentType = contentType;
        }

        /**
         * Content type media type.
         *
         * @return the media type
         */
        @Override
        public MediaType contentType() {
            return contentType;
        }

        /**
         * Content length long.
         *
         * @return the long
         * @throws IOException the io exception
         */
        @Override
        public long contentLength() throws IOException {
            return delegate.contentLength();
        }

        /**
         * Write to.
         *
         * @param sink the sink
         * @throws IOException the io exception
         */
        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            delegate.writeTo(sink);
        }
    }
}
