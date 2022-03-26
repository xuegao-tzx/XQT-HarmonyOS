package com.net.jianjia;

import okhttp3.ResponseBody;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * The type Http service method.
 *
 * @param <ResponseT> the type parameter
 * @param <ReturnT>   the type parameter
 * @modify&fix 田梓萱
 * @date 2022 /2/17
 */
abstract class HttpServiceMethod<ResponseT, ReturnT> extends ServiceMethod<ReturnT> {

    /**
     * The Request factory.
     */
    private final RequestFactory requestFactory;
    /**
     * The Call factory.
     */
    private final okhttp3.Call.Factory callFactory;
    /**
     * The Response converter.
     */
    private final Converter<ResponseBody, ResponseT> responseConverter;

    /**
     * Instantiates a new Http service method.
     *
     * @param requestFactory    the request factory
     * @param callFactory       the call factory
     * @param responseConverter the response converter
     */
    HttpServiceMethod(RequestFactory requestFactory, okhttp3.Call.Factory callFactory,
                      Converter<ResponseBody, ResponseT> responseConverter) {
        this.requestFactory = requestFactory;
        this.callFactory = callFactory;
        this.responseConverter = responseConverter;
    }

    /**
     * Parse annotations http service method.
     *
     * @param <ResponseT>    the type parameter
     * @param <ReturnT>      the type parameter
     * @param jianJia        the jian jia
     * @param method         the method
     * @param requestFactory the request factory
     * @return the http service method
     */
    static <ResponseT, ReturnT> HttpServiceMethod<ResponseT, ReturnT> parseAnnotations(
            JianJia jianJia, Method method, RequestFactory requestFactory) {
        Annotation[] annotations = method.getAnnotations();
        Type adapterType = method.getGenericReturnType();

        CallAdapter<ResponseT, ReturnT> callAdapter =
                createCallAdapter(jianJia, method, adapterType, annotations);
        Type responseType = callAdapter.responseType();
        if (responseType == okhttp3.Response.class) {
            throw Utils.methodError(method, "'"
                    + Utils.getRawType(responseType).getName()
                    + "' is not a valid response body type. Did you mean ResponseBody?");
        }
        if (responseType == Response.class) {
            throw Utils.methodError(method, "Response must include generic type (e.g., Response<String>)");
        }
        if (requestFactory.httpMethod.equals("HEAD") && !Void.class.equals(responseType)) {
            throw Utils.methodError(method, "HEAD method must use Void as response type.");
        }

        Converter<ResponseBody, ResponseT> responseConverter =
                createResponseConverter(jianJia, method, responseType);
        okhttp3.Call.Factory callFactory = jianJia.callFactory;
        return new CallAdapted<>(requestFactory, callFactory, responseConverter, callAdapter);
    }

    /**
     * Create response converter converter.
     *
     * @param <ResponseT>  the type parameter
     * @param jianJia      the jian jia
     * @param method       the method
     * @param responseType the response type
     * @return the converter
     */
    private static <ResponseT> Converter<ResponseBody, ResponseT> createResponseConverter(
            JianJia jianJia, Method method, Type responseType) {
        Annotation[] annotations = method.getAnnotations();
        try {
            return jianJia.responseBodyConverter(responseType, annotations);
        } catch (RuntimeException e) { // Wide exception range because factories are user code.
            throw Utils.methodError(method, e, "Unable to create converter for %s", responseType);
        }
    }

    /**
     * Create call adapter call adapter.
     *
     * @param <ReturnT>   the type parameter
     * @param <ResponseT> the type parameter
     * @param jianJia     the jian jia
     * @param method      the method
     * @param returnType  the return type
     * @param annotations the annotations
     * @return the call adapter
     */
    private static <ReturnT, ResponseT> CallAdapter<ResponseT, ReturnT> createCallAdapter(
            JianJia jianJia, Method method, Type returnType, Annotation[] annotations) {
        try {
            //noinspection unchecked
            return (CallAdapter<ResponseT, ReturnT>) jianJia.callAdapter(returnType, annotations);
        } catch (RuntimeException e) { // Wide exception range because factories are user code.
            throw Utils.methodError(method, e, "Unable to create call adapter for %s", returnType);
        }
    }

    /**
     * Invoke return t.
     *
     * @param args the args
     * @return the return t
     */
    @Override
    public ReturnT invoke(Object[] args) {
        OkHttpCall<ResponseT> call = new OkHttpCall<>(requestFactory, args, callFactory, responseConverter);
        return adapt(call, args);
    }

    /**
     * Adapt return t.
     *
     * @param call the call
     * @param args the args
     * @return the return t
     */
    protected abstract ReturnT adapt(Call<ResponseT> call, Object[] args);

    /**
     * The type Call adapted.
     *
     * @param <ResponseT> the type parameter
     * @param <ReturnT>   the type parameter
     */
    static final class CallAdapted<ResponseT, ReturnT> extends HttpServiceMethod<ResponseT, ReturnT> {

        /**
         * The Call adapter.
         */
        private final CallAdapter<ResponseT, ReturnT> callAdapter;

        /**
         * Instantiates a new Call adapted.
         *
         * @param requestFactory    the request factory
         * @param callFactory       the call factory
         * @param responseConverter the response converter
         * @param callAdapter       the call adapter
         */
        CallAdapted(RequestFactory requestFactory, okhttp3.Call.Factory callFactory,
                    Converter<ResponseBody, ResponseT> responseConverter,
                    CallAdapter<ResponseT, ReturnT> callAdapter) {
            super(requestFactory, callFactory, responseConverter);
            this.callAdapter = callAdapter;
        }

        /**
         * Adapt return t.
         *
         * @param call the call
         * @param args the args
         * @return the return t
         */
        @Override
        protected ReturnT adapt(Call<ResponseT> call, Object[] args) {
            return callAdapter.adapt(call);
        }
    }
}
