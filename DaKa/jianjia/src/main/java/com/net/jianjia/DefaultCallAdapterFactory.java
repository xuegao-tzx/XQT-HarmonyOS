package com.net.jianjia;

import com.net.jianjia.http.SkipCallbackExecutor;
import okhttp3.Request;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.Executor;

/**
 * 默认的类型转换器，如果调用者没有添加{@link CallAdapter}对象，就会使用这个默认的类型转换器
 *
 * @modify&fix 田梓萱
 * @date 2022 /2/17
 */
class DefaultCallAdapterFactory extends CallAdapter.Factory {

    /**
     * The Callback executor.
     */
    private final Executor callbackExecutor;

    /**
     * Instantiates a new Default call adapter factory.
     *
     * @param callbackExecutor the callback executor
     */
    DefaultCallAdapterFactory(Executor callbackExecutor) {
        this.callbackExecutor = callbackExecutor;
    }

    /**
     * Get call adapter.
     *
     * @param returnType  the return type
     * @param annotations the annotations
     * @param jianJia     the jian jia
     * @return the call adapter
     */
    @Override
    public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, JianJia jianJia) {
        if (Utils.getRawType(returnType) != Call.class) return null;
        // 返回值没有泛型
        if (!(returnType instanceof ParameterizedType)) throw new IllegalArgumentException(
                "Call return type must be parameterized as Call<Foo> or Call<? extends Foo>");
        Type responseType = Utils.getParameterUpperBound(0, (ParameterizedType) returnType);
        // executor为空说明不需要将结果回调到主线程
        Executor executor = Utils.isAnnotationPresent(annotations, SkipCallbackExecutor.class) ?
                null : this.callbackExecutor;
        return new CallAdapter<Object, Call<?>>() {

            @Override
            public Type responseType() {
                return responseType;
            }

            @Override
            public Call<?> adapt(Call<Object> call) {
                return executor == null ? call : new ExecutorCallbackCall<>(executor, call);
            }
        };
    }

    /**
     * The type Executor callback call.
     *
     * @param <T> the type parameter
     */
    static final class ExecutorCallbackCall<T> implements Call<T> {

        /**
         * The Callback executor.
         */
        final Executor callbackExecutor;
        /**
         * The Delegate.
         */
        final Call<T> delegate;

        /**
         * Instantiates a new Executor callback call.
         *
         * @param callbackExecutor the callback executor
         * @param delegate         the delegate
         */
        ExecutorCallbackCall(Executor callbackExecutor, Call<T> delegate) {
            this.callbackExecutor = callbackExecutor;
            this.delegate = delegate;
        }

        /**
         * Execute response.
         *
         * @return the response
         * @throws IOException the io exception
         */
        @Override
        public Response<T> execute() throws IOException {
            return this.delegate.execute();
        }

        /**
         * Enqueue.
         *
         * @param callback the callback
         */
        @Override
        public void enqueue(Callback<T> callback) {
            Utils.checkNotNull(callback, "callback == null");
            this.delegate.enqueue(new Callback<T>() {
                @Override
                public void onResponse(Call<T> call, Response<T> response) {
                    ExecutorCallbackCall.this.callbackExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            if (ExecutorCallbackCall.this.delegate.isCanceled())
                                callback.onFailure(ExecutorCallbackCall.this, new IOException("Canceled"));
                            else
                                callback.onResponse(ExecutorCallbackCall.this, response);
                        }
                    });
                }

                @Override
                public void onFailure(Call<T> call, Throwable t) {
                    ExecutorCallbackCall.this.callbackExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFailure(ExecutorCallbackCall.this, t);
                        }
                    });
                }
            });
        }

        /**
         * Is executed boolean.
         *
         * @return the boolean
         */
        @Override
        public boolean isExecuted() {
            return this.delegate.isExecuted();
        }

        /**
         * Cancel.
         */
        @Override
        public void cancel() {
            this.delegate.cancel();
        }

        /**
         * Is canceled boolean.
         *
         * @return the boolean
         */
        @Override
        public boolean isCanceled() {
            return this.delegate.isCanceled();
        }

        /**
         * Clone call.
         *
         * @return the call
         */
        @Override
        public Call<T> clone() {
            return new ExecutorCallbackCall<>(this.callbackExecutor, this.delegate.clone());
        }

        /**
         * Request request.
         *
         * @return the request
         */
        @Override
        public Request request() {
            return this.delegate.request();
        }
    }
}
