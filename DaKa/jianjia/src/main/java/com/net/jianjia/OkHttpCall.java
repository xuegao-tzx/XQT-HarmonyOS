package com.net.jianjia;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;

import java.io.IOException;

/**
 * The type Ok http call.
 *
 * @param <T> the type parameter
 * @modify&fix 田梓萱
 * @date 2022 /2/17
 */
class OkHttpCall<T> implements Call<T> {

    /**
     * The Request factory.
     */
    private final RequestFactory requestFactory;
    /**
     * The Args.
     */
    private final Object[] args;
    /**
     * The Call factory.
     */
    private final okhttp3.Call.Factory callFactory;
    /**
     * The Response converter.
     */
    private final Converter<ResponseBody, T> responseConverter;
    /**
     * The Executed.
     */
    private boolean executed;
    /**
     * The Creation failure.
     */
    private Throwable creationFailure;
    /**
     * The Raw call.
     */
    private okhttp3.Call rawCall;
    /**
     * The Canceled.
     */
    private volatile boolean canceled;

    /**
     * Instantiates a new Ok http call.
     *
     * @param requestFactory    the request factory
     * @param args              the args
     * @param callFactory       the call factory
     * @param responseConverter the response converter
     */
    OkHttpCall(RequestFactory requestFactory, Object[] args,
               okhttp3.Call.Factory callFactory, Converter<ResponseBody, T> responseConverter) {
        this.requestFactory = requestFactory;
        this.args = args;
        this.callFactory = callFactory;
        this.responseConverter = responseConverter;
    }

    /**
     * Execute response.
     *
     * @return the response
     * @throws IOException the io exception
     */
    @Override
    public Response<T> execute() throws IOException {
        okhttp3.Call call;
        synchronized (this) {
            if (executed) {
                throw new IllegalStateException("Already executed.");
            }
            executed = true;
            if (creationFailure != null) {
                if (creationFailure instanceof IOException) {
                    throw (IOException) creationFailure;
                } else if (creationFailure instanceof RuntimeException) {
                    throw (RuntimeException) creationFailure;
                } else {
                    throw (Error) creationFailure;
                }
            }
            call = rawCall;
            if (call == null) {
                try {
                    call = rawCall = createRawCall();
                } catch (IOException | RuntimeException | Error e) {
                    Utils.throwIfFatal(e); //  Do not assign a fatal error to creationFailure.
                    creationFailure = e;
                    throw e;
                }
            }
        }
        if (canceled) {
            call.cancel();
        }
        return parseResponse(call.execute());
    }

    /**
     * Parse response response.
     *
     * @param rawResponse the raw response
     * @return the response
     * @throws IOException the io exception
     */
    Response<T> parseResponse(okhttp3.Response rawResponse) throws IOException {
        ResponseBody rawBody = rawResponse.body();

        // Remove the body's source (the only stateful object) so we can pass the response along.
        rawResponse = rawResponse.newBuilder()
                .body(new NoContentResponseBody(rawBody.contentType(), rawBody.contentLength()))
                .build();

        int code = rawResponse.code();
        if (code < 200 || code >= 300) {
            try {
                // Buffer the entire body to avoid future I/O.
                ResponseBody bufferedBody = Utils.buffer(rawBody);
                return Response.error(bufferedBody, rawResponse);
            } finally {
                rawBody.close();
            }
        }

        if (code == 204 || code == 205) {
            rawBody.close();
            return Response.success(null, rawResponse);
        }

        ExceptionCatchingResponseBody catchingBody = new ExceptionCatchingResponseBody(rawBody);
        try {
            T body = responseConverter.convert(catchingBody);
            return Response.success(body, rawResponse);
        } catch (RuntimeException e) {
            // If the underlying source threw an exception, propagate that rather than indicating it was
            // a runtime exception.
            catchingBody.throwIfCaught();
            throw e;
        }
    }

    /**
     * Create raw call okhttp 3 . call.
     *
     * @return the okhttp 3 . call
     * @throws IOException the io exception
     */
    private okhttp3.Call createRawCall() throws IOException {
        okhttp3.Call call = callFactory.newCall(requestFactory.create(args));
        if (call == null) {
            throw new NullPointerException("Call.Factory returned null.");
        }
        return call;
    }

    /**
     * Enqueue.
     *
     * @param callback the callback
     */
    @Override
    public void enqueue(Callback<T> callback) {
        Utils.checkNotNull(callback, "callback == null");

        okhttp3.Call call;
        Throwable failure;

        synchronized (this) {
            if (executed) throw new IllegalStateException("Already executed.");
            executed = true;

            call = rawCall;
            failure = creationFailure;
            if (call == null && failure == null) {
                try {
                    call = rawCall = createRawCall();
                } catch (Throwable t) {
                    Utils.throwIfFatal(t);
                    failure = creationFailure = t;
                }
            }
        }

        if (failure != null) {
            callback.onFailure(this, failure);
            return;
        }

        if (canceled) {
            call.cancel();
        }

        call.enqueue(new okhttp3.Callback() {
            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response rawResponse) {
                Response<T> response;
                try {
                    response = parseResponse(rawResponse);
                } catch (Throwable e) {
                    Utils.throwIfFatal(e);
                    callFailure(e);
                    return;
                }

                try {
                    callback.onResponse(OkHttpCall.this, response);
                } catch (Throwable t) {
                    Utils.throwIfFatal(t);
                    t.printStackTrace();
                }
            }

            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                callFailure(e);
            }

            private void callFailure(Throwable e) {
                try {
                    callback.onFailure(OkHttpCall.this, e);
                } catch (Throwable t) {
                    Utils.throwIfFatal(t);
                    t.printStackTrace(); // TODO this is not great
                }
            }
        });
    }

    /**
     * Is executed boolean.
     *
     * @return the boolean
     */
    @Override
    public synchronized boolean isExecuted() {
        return executed;
    }

    /**
     * Cancel.
     */
    @Override
    public void cancel() {
        canceled = true;
        okhttp3.Call call;
        synchronized (this) {
            call = rawCall;
        }
        if (call != null) {
            call.cancel();
        }
    }

    /**
     * Is canceled boolean.
     *
     * @return the boolean
     */
    @Override
    public boolean isCanceled() {
        if (canceled) {
            return true;
        }
        synchronized (this) {
            return rawCall != null && rawCall.isCanceled();
        }
    }

    /**
     * Clone call.
     *
     * @return the call
     */
    @Override
    public Call<T> clone() {
        return new OkHttpCall<>(requestFactory, args, callFactory, responseConverter);
    }

    /**
     * Request request.
     *
     * @return the request
     */
    @Override
    public Request request() {
        okhttp3.Call call = rawCall;
        if (call != null) {
            return call.request();
        }
        if (creationFailure != null) {
            if (creationFailure instanceof IOException) {
                throw new RuntimeException("Unable to create request.", creationFailure);
            } else if (creationFailure instanceof RuntimeException) {
                throw (RuntimeException) creationFailure;
            } else {
                throw (Error) creationFailure;
            }
        }
        try {
            return (rawCall = createRawCall()).request();
        } catch (RuntimeException | Error e) {
            Utils.throwIfFatal(e); // Do not assign a fatal error to creationFailure.
            creationFailure = e;
            throw e;
        } catch (IOException e) {
            creationFailure = e;
            throw new RuntimeException("Unable to create request.", e);
        }
    }

    /**
     * The type No content response body.
     */
    static final class NoContentResponseBody extends ResponseBody {
        /**
         * The Content type.
         */
        private final MediaType contentType;
        /**
         * The Content length.
         */
        private final long contentLength;

        /**
         * Instantiates a new No content response body.
         *
         * @param contentType   the content type
         * @param contentLength the content length
         */
        NoContentResponseBody(MediaType contentType, long contentLength) {
            this.contentType = contentType;
            this.contentLength = contentLength;
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
         */
        @Override
        public long contentLength() {
            return contentLength;
        }

        /**
         * Source buffered source.
         *
         * @return the buffered source
         */
        @Override
        public BufferedSource source() {
            throw new IllegalStateException("Cannot read raw response body of a converted body.");
        }
    }

    /**
     * The type Exception catching response body.
     */
    static final class ExceptionCatchingResponseBody extends ResponseBody {
        /**
         * The Delegate.
         */
        private final ResponseBody delegate;
        /**
         * The Delegate source.
         */
        private final BufferedSource delegateSource;
        /**
         * The Thrown exception.
         */
        IOException thrownException;

        /**
         * Instantiates a new Exception catching response body.
         *
         * @param delegate the delegate
         */
        ExceptionCatchingResponseBody(ResponseBody delegate) {
            this.delegate = delegate;
            this.delegateSource = Okio.buffer(new ForwardingSource(delegate.source()) {
                @Override
                public long read(Buffer sink, long byteCount) throws IOException {
                    try {
                        return super.read(sink, byteCount);
                    } catch (IOException e) {
                        thrownException = e;
                        throw e;
                    }
                }
            });
        }

        /**
         * Content type media type.
         *
         * @return the media type
         */
        @Override
        public MediaType contentType() {
            return delegate.contentType();
        }

        /**
         * Content length long.
         *
         * @return the long
         */
        @Override
        public long contentLength() {
            return delegate.contentLength();
        }

        /**
         * Source buffered source.
         *
         * @return the buffered source
         */
        @Override
        public BufferedSource source() {
            return delegateSource;
        }

        /**
         * Close.
         */
        @Override
        public void close() {
            delegate.close();
        }

        /**
         * Throw if caught.
         *
         * @throws IOException the io exception
         */
        void throwIfCaught() throws IOException {
            if (thrownException != null) {
                throw thrownException;
            }
        }
    }
}
