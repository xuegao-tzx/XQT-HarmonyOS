package com.net.jianjia;

import okhttp3.Call;
import okhttp3.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

/**
 * The type Jian jia.
 *
 * @modify&fix 田梓萱
 * @date 2022 /2/17
 */
public class JianJia {

    /**
     * 默认使用OkHttpClient，如果需要对OkHttpClient进行详细的设置，需要构建OkHttpClient对象，然后传入过来
     */
    final Call.Factory callFactory;
    /**
     * 域名
     */
    final HttpUrl baseUrl;
    /**
     * 用于转换数据，可以将返回的responseBody转化为实体对象
     */
    private final List<Converter.Factory> converterFactories;
    /**
     * 对call对象进行转换。默认情况下，接口的方法的返回值为call对象，
     * 如果不想使用call对象，那就可以添加一个callAdapterFactory对象
     */
    private final List<CallAdapter.Factory> callAdapterFactories;
    /**
     * 用于将响应结果回调到主线程
     */
    private final Executor callbackExecutor;
    /**
     * 如果为true，当调用{@link #create}方法时，就会去解析接口里面的所有的非默认非静态的方法。
     * 如果为false，只有当调用接口里面的某个方法时，才会去解析这个方法
     */
    private final boolean validateEagerly;
    /**
     * 接口里面的方法可能会被多次调用，如果不使用缓存，每当调用接口里面的方法时，都会解析方法，这样就做了重复的工作，
     * 这里使用ConcurrentHashMap来缓存已经解析过的方法
     */
    private final Map<Method, ServiceMethod<?>> serviceMethodCache = new ConcurrentHashMap<>();

    /**
     * Instantiates a new Jian jia.
     *
     * @param callFactory          the call factory
     * @param baseUrl              the base url
     * @param converterFactories   the converter factories
     * @param callAdapterFactories the call adapter factories
     * @param callbackExecutor     the callback executor
     * @param validateEagerly      the validate eagerly
     */
    JianJia(Call.Factory callFactory, HttpUrl baseUrl,
            List<Converter.Factory> converterFactories, List<CallAdapter.Factory> callAdapterFactories,
            Executor callbackExecutor, boolean validateEagerly) {
        this.callFactory = callFactory;
        this.baseUrl = baseUrl;
        this.converterFactories = converterFactories;
        this.callAdapterFactories = callAdapterFactories;
        this.callbackExecutor = callbackExecutor;
        this.validateEagerly = validateEagerly;
    }

    /**
     * Create t.
     *
     * @param <T>     the type parameter
     * @param service the service
     * @return the t
     */
    public <T> T create(Class<T> service) {
        Utils.validateServiceInterface(service);
        if (this.validateEagerly) this.eagerlyValidateMethods(service);
        return (T) Proxy.newProxyInstance(
                service.getClassLoader(),
                new Class<?>[]{service},
                new InvocationHandler() {

                    private final Platform platform = Platform.get();
                    private final Object[] emptyArgs = new Object[0];

                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        // Object类中的方法，直接调用
                        if (method.getDeclaringClass() == Object.class) method.invoke(this, args);
                        args = args != null ? args : this.emptyArgs;
                        if (this.platform.isDefaultMethod(method))
                            this.platform.invokeDefaultMethod(method, service, proxy, args);
                        return JianJia.this.loadServiceMethod(method).invoke(args);
                    }
                });
    }

    /**
     * 优先验证接口里面的方法
     *
     * @param service the service
     */
    private void eagerlyValidateMethods(Class<?> service) {
        Platform platform = Platform.get();
        for (Method method : service.getDeclaredMethods())
            if (!platform.isDefaultMethod(method) && !Modifier.isStatic(method.getModifiers()))
                this.loadServiceMethod(method);
    }

    /**
     * 加载接口里面的方法
     *
     * @param method the method
     * @return service method
     */
    private ServiceMethod<?> loadServiceMethod(Method method) {
        ServiceMethod<?> result = this.serviceMethodCache.get(method);
        // 之前已经解析过该方法，不需要再次解析
        if (result != null) return result;
        synchronized (this.serviceMethodCache) {
            result = this.serviceMethodCache.get(method);
            if (result == null) {
                // 之前没有解析过该方法，解析
                result = ServiceMethod.parseAnnotations(this, method);
                // 将解析完的方法放入到缓存
                this.serviceMethodCache.put(method, result);
            }
        }
        return result;
    }

    /**
     * String converter converter.
     *
     * @param <T>         the type parameter
     * @param type        the type
     * @param annotations the annotations
     * @return the converter
     */
    <T> Converter<T, String> stringConverter(Type type, Annotation[] annotations) {
        Utils.checkNotNull(type, "type == null");
        Utils.checkNotNull(annotations, "annotations == null");
        int count = this.converterFactories.size();
        for (int i = 0; i < count; i++) {
            Converter<?, String> converter = this.converterFactories.get(i).stringConverter(type, annotations, this);
            if (converter != null) return (Converter<T, String>) converter;
        }
        return (Converter<T, String>) BuiltInConverters.ToStringConverter.INSTANCE;
    }

    /**
     * Call adapter call adapter.
     *
     * @param returnType  the return type
     * @param annotations the annotations
     * @return the call adapter
     */
    CallAdapter<?, ?> callAdapter(Type returnType, Annotation[] annotations) {
        return this.nextCallAdapter(null, returnType, annotations);
    }

    /**
     * Next call adapter call adapter.
     *
     * @param skipPast    the skip past
     * @param returnType  the return type
     * @param annotations the annotations
     * @return the call adapter
     */
    private CallAdapter<?, ?> nextCallAdapter(CallAdapter.Factory skipPast, Type returnType,
                                              Annotation[] annotations) {
        Utils.checkNotNull(returnType, "returnType == null");
        Utils.checkNotNull(annotations, "annotations == null");
        int start = this.callAdapterFactories.indexOf(skipPast) + 1;
        int count = this.callAdapterFactories.size();
        for (int i = start; i < count; i++) {
            CallAdapter<?, ?> callAdapter = this.callAdapterFactories.get(i).get(returnType, annotations, this);
            if (callAdapter != null) return callAdapter;
        }
        StringBuilder builder = new StringBuilder("Could not locate call adapter for ")
                .append(returnType)
                .append(".\n");
        if (skipPast != null) {
            builder.append("  Skipped:");
            for (int i = 0; i < start; i++)
                builder.append("\n   * ").append(this.callAdapterFactories.get(i).getClass().getName());
            builder.append('\n');
        }
        builder.append("  Tried:");
        for (int i = start; i < count; i++)
            builder.append("\n   * ").append(this.callAdapterFactories.get(i).getClass().getName());
        throw new IllegalArgumentException(builder.toString());
    }

    /**
     * Request body converter converter.
     *
     * @param <T>                  the type parameter
     * @param type                 the type
     * @param parameterAnnotations the parameter annotations
     * @param methodAnnotations    the method annotations
     * @return the converter
     */
    <T> Converter<T, RequestBody> requestBodyConverter(Type type,
                                                       Annotation[] parameterAnnotations, Annotation[] methodAnnotations) {
        return this.nextRequestBodyConverter(null, type, parameterAnnotations, methodAnnotations);
    }

    /**
     * Next request body converter converter.
     *
     * @param <T>                  the type parameter
     * @param skipPast             the skip past
     * @param type                 the type
     * @param parameterAnnotations the parameter annotations
     * @param methodAnnotations    the method annotations
     * @return the converter
     */
    private <T> Converter<T, RequestBody> nextRequestBodyConverter(
            Converter.Factory skipPast, Type type, Annotation[] parameterAnnotations,
            Annotation[] methodAnnotations) {
        Utils.checkNotNull(type, "type == null");
        Utils.checkNotNull(parameterAnnotations, "parameterAnnotations == null");
        Utils.checkNotNull(methodAnnotations, "methodAnnotations == null");

        int start = this.converterFactories.indexOf(skipPast) + 1;
        for (int i = start; i < this.converterFactories.size(); i++) {
            Converter<?, RequestBody> converter = this.converterFactories.get(i).requestBodyConverter(type, parameterAnnotations, methodAnnotations, this);
            if (converter != null) return (Converter<T, RequestBody>) converter;
        }
        StringBuilder builder = new StringBuilder("Could not locate RequestBody converter for ")
                .append(type)
                .append(".\n");
        if (skipPast != null) {
            builder.append("  Skipped:");
            for (int i = 0; i < start; i++)
                builder.append("\n   * ").append(this.converterFactories.get(i).getClass().getName());
            builder.append('\n');
        }
        builder.append("  Tried:");
        for (int i = start, count = this.converterFactories.size(); i < count; i++)
            builder.append("\n   * ").append(this.converterFactories.get(i).getClass().getName());
        throw new IllegalArgumentException(builder.toString());
    }

    /**
     * Response body converter converter.
     *
     * @param <T>         the type parameter
     * @param type        the type
     * @param annotations the annotations
     * @return the converter
     */
    <T> Converter<ResponseBody, T> responseBodyConverter(Type type, Annotation[] annotations) {
        return this.nextResponseBodyConverter(null, type, annotations);
    }

    /**
     * Next response body converter converter.
     *
     * @param <T>         the type parameter
     * @param skipPast    the skip past
     * @param type        the type
     * @param annotations the annotations
     * @return the converter
     */
    private <T> Converter<ResponseBody, T> nextResponseBodyConverter(
            Converter.Factory skipPast, Type type, Annotation[] annotations) {
        Utils.checkNotNull(type, "type == null");
        Utils.checkNotNull(annotations, "annotations == null");
        int start = this.converterFactories.indexOf(skipPast) + 1;
        int count = this.converterFactories.size();
        for (int i = start; i < count; i++) {
            Converter<ResponseBody, ?> converter = this.converterFactories.get(i).responseBodyConverter(type, annotations, this);
            if (converter != null) return (Converter<ResponseBody, T>) converter;
        }

        StringBuilder builder = new StringBuilder("Could not locate ResponseBody converter for ")
                .append(type)
                .append(".\n");
        if (skipPast != null) {
            builder.append("  Skipped:");
            for (int i = 0; i < start; i++)
                builder.append("\n   * ").append(this.converterFactories.get(i).getClass().getName());
            builder.append('\n');
        }
        builder.append("  Tried:");
        for (int i = start; i < count; i++)
            builder.append("\n   * ").append(this.converterFactories.get(i).getClass().getName());
        throw new IllegalArgumentException(builder.toString());
    }

    /**
     * The type Builder.
     */
    public static final class Builder {
        /**
         * The Platform.
         */
        private final Platform platform;
        /**
         * The Converter factories.
         */
        private final List<Converter.Factory> converterFactories = new ArrayList<>();
        /**
         * The Call adapter factories.
         */
        private final List<CallAdapter.Factory> callAdapterFactories = new ArrayList<>();
        /**
         * The Base url.
         */
        private HttpUrl baseUrl;
        /**
         * The Call factory.
         */
        private Call.Factory callFactory;
        /**
         * The Callback executor.
         */
        private Executor callbackExecutor;
        /**
         * The Validate eagerly.
         */
        private boolean validateEagerly;

        /**
         * Instantiates a new Builder.
         */
        public Builder() {
            this(Platform.get());
        }

        /**
         * Instantiates a new Builder.
         *
         * @param platform the platform
         */
        Builder(Platform platform) {
            this.platform = platform;
        }

        /**
         * Base url builder.
         *
         * @param baseUrl the base url
         * @return the builder
         */
        public Builder baseUrl(String baseUrl) {
            Utils.checkNotNull(baseUrl, "baseUrl == null");
            return this.baseUrl(HttpUrl.get(baseUrl));
        }

        /**
         * Base url builder.
         *
         * @param baseUrl the base url
         * @return the builder
         */
        Builder baseUrl(HttpUrl baseUrl) {
            Utils.checkNotNull(baseUrl, "baseUrl == null");
            List<String> pathSegments = baseUrl.pathSegments();
            if (!"".equals(pathSegments.get(pathSegments.size() - 1)))
                throw new IllegalArgumentException("baseUrl must end in /: " + baseUrl);
            this.baseUrl = baseUrl;
            return this;
        }

        /**
         * Client builder.
         *
         * @param client the client
         * @return the builder
         */
        public Builder client(OkHttpClient client) {
            Utils.checkNotNull(client, "client == null");
            return this.callFactory(client);
        }

        /**
         * Call factory builder.
         *
         * @param callFactory the call factory
         * @return the builder
         */
        public Builder callFactory(Call.Factory callFactory) {
            this.callFactory = Utils.checkNotNull(callFactory, "callFactory == null");
            return this;
        }

        /**
         * Add converter factory builder.
         *
         * @param converterFactory the converter factory
         * @return the builder
         */
        public Builder addConverterFactory(Converter.Factory converterFactory) {
            this.converterFactories.add(Utils.checkNotNull(converterFactory, "convertFactory == null"));
            return this;
        }

        /**
         * Add call adapter factory builder.
         *
         * @param callAdapterFactory the call adapter factory
         * @return the builder
         */
        Builder addCallAdapterFactory(CallAdapter.Factory callAdapterFactory) {
            this.callAdapterFactories.add(Utils.checkNotNull(callAdapterFactory, "callAdapterFactory == null"));
            return this;
        }

        /**
         * Callback executor builder.
         *
         * @param callbackExecutor the callback executor
         * @return the builder
         */
        public Builder callbackExecutor(Executor callbackExecutor) {
            this.callbackExecutor = Utils.checkNotNull(callbackExecutor, "callbackExecutor == null");
            return this;
        }

        /**
         * Validate eagerly builder.
         *
         * @param validateEagerly the validate eagerly
         * @return the builder
         */
        public Builder validateEagerly(boolean validateEagerly) {
            this.validateEagerly = validateEagerly;
            return this;
        }

        /**
         * Converter factories list.
         *
         * @return the list
         */
        public List<Converter.Factory> converterFactories() {
            return this.converterFactories;
        }

        /**
         * Call adapter factories list.
         *
         * @return the list
         */
        public List<CallAdapter.Factory> callAdapterFactories() {
            return this.callAdapterFactories;
        }

        /**
         * Build jian jia.
         *
         * @return the jian jia
         */
        public JianJia build() {
            if (this.baseUrl == null) throw new IllegalArgumentException("base url required");
            Call.Factory callFactory = this.callFactory;
            if (callFactory == null) callFactory = new OkHttpClient();
            Executor callbackExecutor = this.callbackExecutor;
            if (callbackExecutor == null) callbackExecutor = this.platform.defaultCallbackExecutor();
            List<CallAdapter.Factory> callAdapterFactories = new ArrayList<>(this.callAdapterFactories);
            callAdapterFactories.addAll(this.platform.defaultCallAdapterFactories(callbackExecutor));

            List<Converter.Factory> convertFactories = new ArrayList<>(
                    1 + this.converterFactories.size() + this.platform.defaultConverterFactoriesSize());
            convertFactories.add(new BuiltInConverters());
            convertFactories.addAll(this.converterFactories);
            convertFactories.addAll(this.platform.defaultConverterFactories());

            return new JianJia(callFactory, this.baseUrl, Collections.unmodifiableList(convertFactories),
                    Collections.unmodifiableList(callAdapterFactories), callbackExecutor, this.validateEagerly);
        }
    }
}
