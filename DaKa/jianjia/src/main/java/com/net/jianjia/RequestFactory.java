package com.net.jianjia;

import com.net.jianjia.http.*;
import okhttp3.Headers;
import okhttp3.*;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.net.jianjia.Utils.methodError;
import static com.net.jianjia.Utils.parameterError;


/**
 * The type Request factory.
 *
 * @modify&fix 田梓萱
 * @date 2022 /2/17
 */
final class RequestFactory {

    /**
     * 请求方法
     */
    final String httpMethod;
    /**
     * 相对路径
     */
    private final String relativeUrl;
    /**
     * The Method.
     */
    private final Method method;
    /**
     * 请求的域名，如果没有在方法上使用{@link BaseUrl @BaseUrl}注解，在请求服务端的时候就会使用这个变量
     */
    private final HttpUrl httpUrl;
    /**
     * 请求的域名，如果在方法上使用了{@link BaseUrl @BaseUrl}注解，在请求服务端的时候就会使用这个变量
     */
    private final HttpUrl newHttpUrl;
    /**
     * 请求头
     */
    private final Headers headers;
    /**
     * The Content type.
     */
    private final MediaType contentType;
    /**
     * The Has body.
     */
    private final boolean hasBody;
    /**
     * 是否使用了{@link FormUrlEncoded}注解
     */
    private final boolean isFormEncoded;
    /**
     * 是否使用了{@link Multipart}注解
     */
    private final boolean isMultipart;
    /**
     * 用于保存在参数声明的注解
     */
    private final ParameterHandler<?>[] parameterHandlers;

    /**
     * Instantiates a new Request factory.
     *
     * @param builder the builder
     */
    RequestFactory(Builder builder) {
        this.method = builder.method;
        this.httpUrl = builder.jianJia.baseUrl;
        this.newHttpUrl = builder.newBaseUrl;
        this.httpMethod = builder.httpMethod;
        this.relativeUrl = builder.relativeUrl;
        this.headers = builder.headers;
        this.contentType = builder.contentType;
        this.hasBody = builder.hasBody;
        this.isFormEncoded = builder.isFormEncoded;
        this.isMultipart = builder.isMultipart;
        this.parameterHandlers = builder.parameterHandlers;
    }

    /**
     * 解析完方法上的注解和参数上的注解后，使用构造者模式对变量进行复制
     *
     * @param jianjia the jianjia
     * @param method  the method
     * @return request factory
     */
    static RequestFactory parseAnnotations(JianJia jianjia, Method method) {
        return new Builder(jianjia, method).build();
    }

    /**
     * 创建请求对象，使用OKHTTP的request对象
     *
     * @param args the args
     * @return okhttp 3 . request
     * @throws IOException the io exception
     */
    okhttp3.Request create(Object[] args) throws IOException {
        ParameterHandler<Object>[] handlers = (ParameterHandler<Object>[]) this.parameterHandlers;
        int argumentCount = args.length;
        if (argumentCount != handlers.length) throw new IllegalArgumentException("Argument count (" + argumentCount
                + ") doesn't match expected count (" + handlers.length + ")");
        RequestBuilder requestBuilder = new RequestBuilder(this.httpMethod, this.httpUrl, this.newHttpUrl, this.relativeUrl, this.headers,
                this.contentType, this.hasBody, this.isFormEncoded, this.isMultipart);
        List<Object> argumentList = new ArrayList<>(argumentCount);
        for (int i = 0; i < argumentCount; i++) {
            argumentList.add(args[i]);
            handlers[i].apply(requestBuilder, args[i]);
        }
        return requestBuilder.get()
                .tag(Invocation.class, Invocation.of(this.method, argumentList))
                .build();
    }

    /**
     * The type Builder.
     */
    static final class Builder {
        /**
         * The constant PARAM.
         */
// 以字符开头的大小写字母、数字、下划线和连字符。
        private static final String PARAM = "[a-zA-Z][a-zA-Z0-9_-]*";
        /**
         * The constant PARAM_URL_REGEX.
         */
        private static final Pattern PARAM_URL_REGEX = Pattern.compile("\\{(" + PARAM + ")\\}");
        /**
         * The constant PARAM_NAME_REGEX.
         */
        private static final Pattern PARAM_NAME_REGEX = Pattern.compile(PARAM);

        /**
         * The Jian jia.
         */
        final JianJia jianJia;
        /**
         * The Method.
         */
        final Method method;
        /**
         * 方法上的注解
         */
        final Annotation[] methodAnnotations;
        /**
         * 方法参数上面的注解
         */
        final Annotation[][] parameterAnnotationsArray;
        /**
         * 方法参数的类型
         */
        final Type[] parameterTypes;

        /**
         * The Got field.
         */
        boolean gotField;
        /**
         * The Got part.
         */
        boolean gotPart;
        /**
         * The Got body.
         */
        boolean gotBody;
        /**
         * The Got path.
         */
        boolean gotPath;
        /**
         * The Got query.
         */
        boolean gotQuery;
        /**
         * The Got query name.
         */
        boolean gotQueryName;
        /**
         * The Got query map.
         */
        boolean gotQueryMap;
        /**
         * The Got url.
         */
        boolean gotUrl;

        /**
         * 请求方法
         */
        String httpMethod;
        /**
         * The Has body.
         */
        boolean hasBody;
        /**
         * The Is form encoded.
         */
        boolean isFormEncoded;
        /**
         * The Is multipart.
         */
        boolean isMultipart;
        /**
         * 相对路径
         */
        String relativeUrl;
        /**
         * 相对路径里面会带有参数，这个就是参数的集合
         */
        Set<String> relativeUrlParamNames;
        /**
         * 请求头
         */
        Headers headers;
        /**
         * The Content type.
         */
        MediaType contentType;
        /**
         * 用于保存参数的注解
         */
        ParameterHandler<?>[] parameterHandlers;
        /**
         * 如果在方法上使用了{@link BaseUrl @BaseUrl}注解，这个参数就不会为空
         */
        HttpUrl newBaseUrl;

        /**
         * Instantiates a new Builder.
         *
         * @param jianJia the jian jia
         * @param method  the method
         */
        Builder(JianJia jianJia, Method method) {
            this.jianJia = jianJia;
            this.method = method;
            this.methodAnnotations = method.getAnnotations();
            this.parameterTypes = method.getGenericParameterTypes();
            this.parameterAnnotationsArray = method.getParameterAnnotations();
        }

        /**
         * 假设需要解析这样的相对路径/repos/{owner}/{repo}/contributors，
         * 这个方法就是将owner和repo放入集合
         *
         * @param path /repos/{owner}/{repo}/contributors
         * @return set set
         */
        static Set<String> parsePathParameters(String path) {
            Matcher m = PARAM_URL_REGEX.matcher(path);
            Set<String> patterns = new LinkedHashSet<>();
            while (m.find()) patterns.add(m.group(1));
            return patterns;
        }

        /**
         * 判断基本数据类型
         *
         * @param type the type
         * @return class class
         */
        private static Class<?> boxIfPrimitive(Class<?> type) {
            if (boolean.class == type) return Boolean.class;
            if (byte.class == type) return Byte.class;
            if (char.class == type) return Character.class;
            if (double.class == type) return Double.class;
            if (float.class == type) return Float.class;
            if (int.class == type) return Integer.class;
            if (long.class == type) return Long.class;
            if (short.class == type) return Short.class;
            return type;
        }

        /**
         * Build request factory.
         *
         * @return the request factory
         */
        RequestFactory build() {
            for (Annotation annotation : this.methodAnnotations) this.parseMethodAnnotation(annotation);
            // 解析完注解后，需要判断是否提供了请求方法
            if (this.httpMethod == null)
                throw methodError(this.method, "HTTP method annotation is required (e.g., @GET, @POST, etc.).");
            if (!this.hasBody) {
                if (this.isMultipart) throw methodError(this.method,
                        "Multipart can only be specified on HTTP methods with request body (e.g., @POST).");
                if (this.isFormEncoded)
                    throw methodError(this.method, "FormUrlEncoded can only be specified on HTTP methods with "
                            + "request body (e.g., @POST).");
            }
            // 参数注解的个数
            int parameterCount = this.parameterAnnotationsArray.length;
            this.parameterHandlers = new ParameterHandler<?>[parameterCount];
            // 解析参数上的注解
            for (int p = 0; p < parameterCount; p++)
                this.parameterHandlers[p] = this.parseParameter(p, this.parameterTypes[p], this.parameterAnnotationsArray[p]);

            if (this.relativeUrl == null && !this.gotUrl)
                throw methodError(this.method, "Missing either @%s URL or @Url parameter.", this.httpMethod);
            if (!this.isFormEncoded && !this.isMultipart && !this.hasBody && this.gotBody)
                throw methodError(this.method, "Non-body HTTP method cannot contain @Body.");
            if (this.isFormEncoded && !this.gotField)
                throw methodError(this.method, "Form-encoded method must contain at least one @Field.");
            if (this.isMultipart && !this.gotPart)
                throw methodError(this.method, "Multipart method must contain at least one @Part.");
            return new RequestFactory(this);
        }

        /**
         * Parse parameter parameter handler.
         *
         * @param p           the p
         * @param type        the type
         * @param annotations the annotations
         * @return the parameter handler
         */
        private ParameterHandler<?> parseParameter(int p, Type type, Annotation[] annotations) {
            ParameterHandler<?> result = null;
            if (annotations != null) for (Annotation annotation : annotations) {
                ParameterHandler<?> annotationAction =
                        this.parseParameterAnnotation(p, type, annotations, annotation);
                if (annotation == null) continue;
                if (result != null) throw parameterError(this.method, p,
                        "Multiple Retrofit annotations found, only one allowed.");
                result = annotationAction;
            }
            if (result == null) throw parameterError(this.method, p, "No Retrofit annotation found.");
            return result;
        }

        /**
         * Parse parameter annotation parameter handler.
         *
         * @param p           the p
         * @param type        the type
         * @param annotations the annotations
         * @param annotation  the annotation
         * @return the parameter handler
         */
        private ParameterHandler<?> parseParameterAnnotation(int p, Type type, Annotation[] annotations, Annotation annotation) {
            if (annotation instanceof Url) return this.parseUrl(p, type, annotations, annotation);
            else if (annotation instanceof Path) return this.parsePath(p, type, annotations, annotation);
            else if (annotation instanceof Query) return this.parseQuery(p, type, annotations, annotation);
            else if (annotation instanceof QueryMap) return this.parseQueryMap(p, type, annotations, annotation);
            else if (annotation instanceof Header) return this.parseHeader(p, type, annotations, annotation);
            else if (annotation instanceof HeaderMap) return this.parseHeaderMap(p, type, annotations, annotation);
            else if (annotation instanceof Field) return this.parseField(p, type, annotations, annotation);
            else if (annotation instanceof FieldMap) return this.parseFieldMap(p, type, annotations, annotation);
            else if (annotation instanceof Body) return this.parseBody(p, type, annotations, annotation);
            else if (annotation instanceof Part) return this.parsePart(p, type, annotations, annotation);
            else if (annotation instanceof PartMap) return this.parsePartMap(p, type, annotations, annotation);
            return null;
        }

        /**
         * Parse part map parameter handler.
         *
         * @param p           the p
         * @param type        the type
         * @param annotations the annotations
         * @param annotation  the annotation
         * @return the parameter handler
         */
        private ParameterHandler<?> parsePartMap(int p, Type type, Annotation[] annotations, Annotation annotation) {
            this.validateResolvableType(p, type);
            if (!this.isMultipart) throw parameterError(this.method, p,
                    "@PartMap parameters can only be used with multipart encoding.");
            this.gotPart = true;
            Class<?> rawParameterType = Utils.getRawType(type);
            if (!Map.class.isAssignableFrom(rawParameterType))
                throw parameterError(this.method, p, "@PartMap parameter type must be Map.");
            Type mapType = Utils.getSupertype(type, rawParameterType, Map.class);
            if (!(mapType instanceof ParameterizedType)) throw parameterError(this.method, p,
                    "Map must include generic types (e.g., Map<String, String>)");
            ParameterizedType parameterizedType = (ParameterizedType) mapType;

            Type keyType = Utils.getParameterUpperBound(0, parameterizedType);
            if (String.class != keyType)
                throw parameterError(this.method, p, "@PartMap keys must be of type String: " + keyType);

            Type valueType = Utils.getParameterUpperBound(1, parameterizedType);
            if (MultipartBody.Part.class.isAssignableFrom(Utils.getRawType(valueType)))
                throw parameterError(this.method, p, "@PartMap values cannot be MultipartBody.Part. "
                        + "Use @Part List<Part> or a different value type instead.");

            Converter<?, RequestBody> valueConverter =
                    this.jianJia.requestBodyConverter(valueType, annotations, this.methodAnnotations);

            PartMap partMap = (PartMap) annotation;
            return new ParameterHandler.PartMap<>(this.method, p, valueConverter, partMap.encoding());

        }

        /**
         * Parse part parameter handler.
         *
         * @param p           the p
         * @param type        the type
         * @param annotations the annotations
         * @param annotation  the annotation
         * @return the parameter handler
         */
        private ParameterHandler<?> parsePart(int p, Type type, Annotation[] annotations, Annotation annotation) {
            this.validateResolvableType(p, type);
            if (!this.isMultipart) throw parameterError(this.method, p,
                    "@Part parameters can only be used with multipart encoding.");
            Part part = (Part) annotation;
            this.gotPart = true;

            String partName = part.value();
            Class<?> rawParameterType = Utils.getRawType(type);
            if (partName.isEmpty()) if (Iterable.class.isAssignableFrom(rawParameterType)) {
                if (!(type instanceof ParameterizedType))
                    throw parameterError(this.method, p, rawParameterType.getSimpleName()
                            + " must include generic type (e.g., "
                            + rawParameterType.getSimpleName()
                            + "<String>)");
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Type iterableType = Utils.getParameterUpperBound(0, parameterizedType);
                if (!MultipartBody.Part.class.isAssignableFrom(Utils.getRawType(iterableType)))
                    throw parameterError(this.method, p,
                            "@Part annotation must supply a name or use MultipartBody.Part parameter type.");
                return ParameterHandler.RawPart.INSTANCE.iterable();
            } else if (rawParameterType.isArray()) {
                Class<?> arrayComponentType = rawParameterType.getComponentType();
                if (!MultipartBody.Part.class.isAssignableFrom(arrayComponentType))
                    throw parameterError(this.method, p,
                            "@Part annotation must supply a name or use MultipartBody.Part parameter type.");
                return ParameterHandler.RawPart.INSTANCE.array();
            } else if (MultipartBody.Part.class.isAssignableFrom(rawParameterType))
                return ParameterHandler.RawPart.INSTANCE;
            else throw parameterError(this.method, p,
                        "@Part annotation must supply a name or use MultipartBody.Part parameter type.");
            else {
                Headers headers =
                        Headers.of("Content-Disposition", "form-data; name=\"" + partName + "\"",
                                "Content-Transfer-Encoding", part.encoding());

                if (Iterable.class.isAssignableFrom(rawParameterType)) {
                    if (!(type instanceof ParameterizedType))
                        throw parameterError(this.method, p, rawParameterType.getSimpleName()
                                + " must include generic type (e.g., "
                                + rawParameterType.getSimpleName()
                                + "<String>)");
                    ParameterizedType parameterizedType = (ParameterizedType) type;
                    Type iterableType = Utils.getParameterUpperBound(0, parameterizedType);
                    if (MultipartBody.Part.class.isAssignableFrom(Utils.getRawType(iterableType)))
                        throw parameterError(this.method, p,
                                "@Part parameters using the MultipartBody.Part must not "
                                        + "include a part name in the annotation.");
                    Converter<?, RequestBody> converter =
                            this.jianJia.requestBodyConverter(iterableType, annotations, this.methodAnnotations);
                    return new ParameterHandler.Part<>(this.method, p, headers, converter).iterable();
                } else if (rawParameterType.isArray()) {
                    Class<?> arrayComponentType = boxIfPrimitive(rawParameterType.getComponentType());
                    if (MultipartBody.Part.class.isAssignableFrom(arrayComponentType))
                        throw parameterError(this.method, p,
                                "@Part parameters using the MultipartBody.Part must not "
                                        + "include a part name in the annotation.");
                    Converter<?, RequestBody> converter =
                            this.jianJia.requestBodyConverter(arrayComponentType, annotations, this.methodAnnotations);
                    return new ParameterHandler.Part<>(this.method, p, headers, converter).array();
                } else if (MultipartBody.Part.class.isAssignableFrom(rawParameterType))
                    throw parameterError(this.method, p,
                            "@Part parameters using the MultipartBody.Part must not "
                                    + "include a part name in the annotation.");
                else {
                    Converter<?, RequestBody> converter =
                            this.jianJia.requestBodyConverter(type, annotations, this.methodAnnotations);
                    return new ParameterHandler.Part<>(this.method, p, headers, converter);
                }
            }

        }

        /**
         * 解析{@link Body}注解，内部会把该实体序列化并将序列化后的结果直接作为请求体发送出去。
         * 如果被body注解修饰的参数的类型是RequestBody对象，那调用者可以不添加数据转换器，内部会使用默认的数据转换器
         * 如果被body注解修饰的参数的类型不是RequestBody对象，是一个具体的实体类，那调用者需要自定义一个类，并且继承{@link Converter.Factory}
         *
         * @param p           the p
         * @param type        the type
         * @param annotations the annotations
         * @param annotation  the annotation
         * @return parameter handler
         */
        private ParameterHandler<?> parseBody(int p, Type type, Annotation[] annotations, Annotation annotation) {
            this.validateResolvableType(p, type);
            if (this.isFormEncoded || this.isMultipart) throw parameterError(this.method, p,
                    "@Body parameters cannot be used with form or multi-part encoding.");
            if (this.gotBody) throw parameterError(this.method, p, "Multiple @Body method annotations found.");
            Converter<?, RequestBody> converter;
            try {
                converter = this.jianJia.requestBodyConverter(type, annotations, this.methodAnnotations);
            } catch (RuntimeException e) {
                throw parameterError(this.method, e, p, "Unable to create @Body converter for %s", type);
            }
            this.gotBody = true;
            return new ParameterHandler.Body<>(this.method, p, converter);
        }

        /**
         * 解析{@link FieldMap}注解，{@link FieldMap}注解用于map的形式发送一个表单请求。
         * 如果被{@link FieldMap}注解修饰的参数不是{@link Map}类型，就会抛异常。
         * 如果{@link Map}的键值对为空，也会抛异常
         *
         * @param p           the p
         * @param type        the type
         * @param annotations the annotations
         * @param annotation  the annotation
         * @return parameter handler
         */
        private ParameterHandler<?> parseFieldMap(int p, Type type, Annotation[] annotations, Annotation annotation) {
            this.validateResolvableType(p, type);
            if (!this.isFormEncoded) throw parameterError(this.method, p,
                    "@FieldMap parameters can only be used with form encoding.");
            Class<?> rawParameterType = Utils.getRawType(type);
            if (!Map.class.isAssignableFrom(rawParameterType))
                throw parameterError(this.method, p, "@FieldMap parameter type must be Map.");
            Type mapType = Utils.getSupertype(type, rawParameterType, Map.class);
            if (!(mapType instanceof ParameterizedType)) throw parameterError(this.method, p,
                    "Map must include generic types (e.g., Map<String, String>)");
            ParameterizedType parameterizedType = (ParameterizedType) mapType;
            Type keyType = Utils.getParameterUpperBound(0, parameterizedType);
            if (String.class != keyType)
                throw parameterError(this.method, p, "@FieldMap keys must be of type String: " + keyType);
            Type valueType = Utils.getParameterUpperBound(1, parameterizedType);
            Converter<?, String> valueConverter =
                    this.jianJia.stringConverter(valueType, annotations);
            this.gotField = true;
            return new ParameterHandler.FieldMap<>(this.method, p,
                    valueConverter, ((FieldMap) annotation).encoded());

        }

        /**
         * 解析{@link Field}注解，{@link Field}注解用于发送一个表单请求。
         * 被{@link Field}注解修饰的参数类型可以是数组、集合、字符串等
         *
         * @param p           the p
         * @param type        the type
         * @param annotations the annotations
         * @param annotation  the annotation
         * @return parameter handler
         */
        private ParameterHandler<?> parseField(int p, Type type, Annotation[] annotations, Annotation annotation) {
            this.validateResolvableType(p, type);
            // Field注解必须与FormUrlEncoded一起使用
            if (!this.isFormEncoded)
                throw parameterError(this.method, p, "@Field parameters can only be used with form encoding.");
            Field field = (Field) annotation;
            String name = field.value();
            boolean encoded = field.encoded();
            Class<?> rawParameterType = Utils.getRawType(type);
            this.gotField = true;
            if (Iterable.class.isAssignableFrom(rawParameterType)) {
                // rawParameterType实现了Iterable接口，比如ArrayList就实现了Iterable接口
                // 方法的参数类型没有泛型
                if (!(type instanceof ParameterizedType))
                    throw parameterError(this.method, p, rawParameterType.getSimpleName()
                            + " must include generic type (e.g., "
                            + rawParameterType.getSimpleName()
                            + "<String>)");
                ParameterizedType parameterizedType = (ParameterizedType) type;
                // 获取参数类型的泛型
                Type iterableType = Utils.getParameterUpperBound(0, parameterizedType);
                Converter<?, String> converter = this.jianJia.stringConverter(iterableType, annotations);
                return new ParameterHandler.Field<>(name, converter, encoded).iterable();
            } else if (rawParameterType.isArray()) {
                // 方法的参数类型是数组
                Class<?> arrayComponentType = boxIfPrimitive(rawParameterType.getComponentType());
                Converter<?, String> converter = this.jianJia.stringConverter(arrayComponentType, annotations);
                return new ParameterHandler.Field<>(name, converter, encoded).array();
            } else {
                // 方法的参数类型不是集合也不是数组
                Converter<?, String> converter = this.jianJia.stringConverter(rawParameterType, annotations);
                return new ParameterHandler.Field<>(name, converter, encoded);
            }
        }

        /**
         * Parse header map parameter handler.
         *
         * @param p           the p
         * @param type        the type
         * @param annotations the annotations
         * @param annotation  the annotation
         * @return the parameter handler
         */
        private ParameterHandler<?> parseHeaderMap(int p, Type type, Annotation[] annotations, Annotation annotation) {
            this.validateResolvableType(p, type);
            Class<?> rawParameterType = Utils.getRawType(type);
            if (!Map.class.isAssignableFrom(rawParameterType))
                throw parameterError(this.method, p, "@HeaderMap parameter type must be Map.");
            Type mapType = Utils.getSupertype(type, rawParameterType, Map.class);
            // map没有泛型
            if (!(mapType instanceof ParameterizedType)) throw parameterError(this.method, p,
                    "Map must include generic types (e.g., Map<String, String>)");
            ParameterizedType parameterizedType = (ParameterizedType) mapType;
            Type keyType = Utils.getParameterUpperBound(0, parameterizedType);
            if (String.class != keyType)
                throw parameterError(this.method, p, "@HeaderMap keys must be of type String: " + keyType);
            Type valueType = Utils.getParameterUpperBound(1, parameterizedType);
            Converter<?, String> converter = this.jianJia.stringConverter(valueType, annotations);
            return new ParameterHandler.HeaderMap<>(this.method, p, converter);
        }

        /**
         * 解析{@link Header}注解，{@link Header}注解用于添加请求头
         * 被{@link Header}注解修饰的参数类型可以是数组、集合、字符串等
         *
         * @param p           the p
         * @param type        the type
         * @param annotations the annotations
         * @param annotation  the annotation
         * @return parameter handler
         */
        private ParameterHandler<?> parseHeader(int p, Type type, Annotation[] annotations, Annotation annotation) {
            this.validateResolvableType(p, type);
            Header header = (Header) annotation;
            String name = header.value();
            Class<?> rawParameterType = Utils.getRawType(type);
            if (Iterable.class.isAssignableFrom(rawParameterType)) {
                // rawParameterType实现了Iterable接口，比如ArrayList就实现了Iterable接口
                // 方法的参数类型没有泛型
                if (!(type instanceof ParameterizedType))
                    throw parameterError(this.method, p, rawParameterType.getSimpleName()
                            + " must include generic type (e.g., "
                            + rawParameterType.getSimpleName()
                            + "<String>)");
                ParameterizedType parameterizedType = (ParameterizedType) type;
                // 获取参数类型的泛型
                Type iterableType = Utils.getParameterUpperBound(0, parameterizedType);
                Converter<?, String> converter = this.jianJia.stringConverter(iterableType, annotations);
                return new ParameterHandler.Header<>(name, converter).iterable();
            } else if (rawParameterType.isArray()) {
                // 方法的参数类型是数组
                Class<?> arrayComponentType = boxIfPrimitive(rawParameterType.getComponentType());
                Converter<?, String> converter = this.jianJia.stringConverter(arrayComponentType, annotations);
                return new ParameterHandler.Header<>(name, converter).array();
            } else {
                // 方法的参数类型不是集合也不是数组
                Converter<?, String> converter = this.jianJia.stringConverter(rawParameterType, annotations);
                return new ParameterHandler.Header<>(name, converter);
            }
        }

        /**
         * 解析{@link QueryMap}注解，{@link QueryMap}注解以{@link Map}的形式添加查询参数
         *
         * @param p           the p
         * @param type        the type
         * @param annotations the annotations
         * @param annotation  the annotation
         * @return parameter handler
         */
        private ParameterHandler<?> parseQueryMap(int p, Type type, Annotation[] annotations, Annotation annotation) {
            this.validateResolvableType(p, type);
            Class<?> rawParameterType = Utils.getRawType(type);
            this.gotQueryMap = true;
            if (!Map.class.isAssignableFrom(rawParameterType))
                throw parameterError(this.method, p, "@QueryMap parameter type must be Map.");
            Type mapType = Utils.getSupertype(type, rawParameterType, Map.class);
            // map没有泛型
            if (!(mapType instanceof ParameterizedType)) throw parameterError(this.method, p,
                    "Map must include generic types (e.g., Map<String, String>)");
            ParameterizedType parameterizedType = (ParameterizedType) mapType;
            Type keyType = Utils.getParameterUpperBound(0, parameterizedType);
            if (String.class != keyType)
                throw parameterError(this.method, p, "@QueryMap keys must be of type String: " + keyType);
            Type valueType = Utils.getParameterUpperBound(1, parameterizedType);
            Converter<?, String> converter = this.jianJia.stringConverter(valueType, annotations);
            return new ParameterHandler.QueryMap<>(this.method, p, converter, ((QueryMap) annotation).encoded());
        }

        /**
         * 解析{@link Query}注解，{@link Query}注解用于给get请求添加请求参数
         * 被{@link Query}注解修饰的参数类型可以是数组、集合、字符串等
         *
         * @param p           the p
         * @param type        方法参数的类型
         * @param annotations the annotations
         * @param annotation  the annotation
         * @return parameter handler
         */
        private ParameterHandler<?> parseQuery(int p, Type type, Annotation[] annotations, Annotation annotation) {
            this.validateResolvableType(p, type);
            Query query = (Query) annotation;
            String name = query.value();
            boolean encoded = query.encoded();
            Class<?> rawParameterType = Utils.getRawType(type);
            this.gotQuery = true;
            if (Iterable.class.isAssignableFrom(rawParameterType)) {
                // rawParameterType实现了Iterable接口，比如ArrayList就实现了Iterable接口
                // 方法的参数类型没有泛型
                if (!(type instanceof ParameterizedType))
                    throw parameterError(this.method, p, rawParameterType.getSimpleName()
                            + " must include generic type (e.g., "
                            + rawParameterType.getSimpleName()
                            + "<String>)");
                ParameterizedType parameterizedType = (ParameterizedType) type;
                // 获取参数类型的泛型
                Type iterableType = Utils.getParameterUpperBound(0, parameterizedType);
                Converter<?, String> converter = this.jianJia.stringConverter(iterableType, annotations);
                return new ParameterHandler.Query<>(name, converter, encoded).iterable();
            } else if (rawParameterType.isArray()) {
                // 方法的参数类型是数组
                Class<?> arrayComponentType = boxIfPrimitive(rawParameterType.getComponentType());
                Converter<?, String> converter = this.jianJia.stringConverter(arrayComponentType, annotations);
                return new ParameterHandler.Query<>(name, converter, encoded).array();
            } else {
                // 方法的参数类型不是集合也不是数组
                Converter<?, String> converter = this.jianJia.stringConverter(rawParameterType, annotations);
                return new ParameterHandler.Query<>(name, converter, encoded);
            }
        }

        /**
         * 解析{@link Path}注解，{@link Path}注解在URL路径段中替换指定的参数值
         *
         * @param p           the p
         * @param type        方法参数的类型
         * @param annotations the annotations
         * @param annotation  the annotation
         * @return parameter handler
         */
        private ParameterHandler<?> parsePath(int p, Type type, Annotation[] annotations, Annotation annotation) {
            this.validateResolvableType(p, type);
            if (this.gotQuery) throw parameterError(this.method, p, "A @Path parameter must not come after a @Query.");
            if (this.gotQueryName)
                throw parameterError(this.method, p, "A @Path parameter must not come after a @QueryName.");
            if (this.gotQueryMap)
                throw parameterError(this.method, p, "A @Path parameter must not come after a @QueryMap.");
            if (this.gotUrl) throw parameterError(this.method, p, "@Path parameters may not be used with @Url.");
            if (this.relativeUrl == null)
                throw parameterError(this.method, p, "@Path can only be used with relative url on @%s",
                        this.httpMethod);
            this.gotPart = true;
            Path path = (Path) annotation;
            String name = path.value();
            this.validatePathName(p, name);
            Converter<?, String> converter = this.jianJia.stringConverter(type, annotations);
            return new ParameterHandler.Path<>(this.method, p, name, converter, path.encoded());
        }

        /**
         * 解析{@link Url}注解，{@link Url}注解用于添加请求的接口地址
         * 被{@link Url}注解修饰的参数类型可以是HttpUrl，String，URI，ohos.utils.net.Uri等
         *
         * @param p           the p
         * @param type        the type
         * @param annotations the annotations
         * @param annotation  the annotation
         * @return parameter handler
         */
        private ParameterHandler<?> parseUrl(int p, Type type, Annotation[] annotations, Annotation annotation) {
            this.validateResolvableType(p, type);
            if (this.gotUrl) throw parameterError(this.method, p, "Multiple @Url method annotations found.");
            if (this.gotPath) throw parameterError(this.method, p, "@Path parameters may not be used with @Url.");
            if (this.gotQuery) throw parameterError(this.method, p, "A @Url parameter must not come after a @Query.");
            if (this.gotQueryName)
                throw parameterError(this.method, p, "A @Url parameter must not come after a @QueryName.");
            if (this.gotQueryMap)
                throw parameterError(this.method, p, "A @Url parameter must not come after a @QueryMap.");
            if (this.relativeUrl != null)
                throw parameterError(this.method, p, "@Url cannot be used with @%s URL", this.httpMethod);
            this.gotUrl = true;
            if (type == HttpUrl.class
                    || type == String.class
                    || type == URI.class
                    || (type instanceof Class && "ohos.utils.net.Uri".equals(((Class<?>) type).getName())))
                return new ParameterHandler.RelativeUrl(this.method, p);
            else throw parameterError(this.method, p,
                    "@Url must be okhttp3.HttpUrl, String, java.net.URI, or ohos.utils.net.Uri type.");
        }

        /**
         * Validate path name.
         *
         * @param p    the p
         * @param name the name
         */
        private void validatePathName(int p, String name) {
            if (!PARAM_NAME_REGEX.matcher(name).matches())
                throw parameterError(this.method, p, "@Path parameter name must match %s. Found: %s",
                        PARAM_URL_REGEX.pattern(), name);
            // 验证URL路径中是否确实存在URL替换名称
            if (!this.relativeUrlParamNames.contains(name))
                throw parameterError(this.method, p, "URL \"%s\" does not contain \"{%s}\".", this.relativeUrl, name);
        }

        /**
         * Validate resolvable type.
         *
         * @param p    the p
         * @param type the type
         */
        private void validateResolvableType(int p, Type type) {
            if (Utils.hasUnresolvableType(type)) throw parameterError(this.method, p,
                    "Parameter type must not include a type variable or wildcard: %s", type);
        }

        /**
         * 解析方法上的注解
         *
         * @param annotation 注解
         */
        private void parseMethodAnnotation(Annotation annotation) {
            if (annotation instanceof DELETE)
                this.parseHttpMethodAndPath("DELETE", ((DELETE) annotation).value(), false);
            else if (annotation instanceof GET) this.parseHttpMethodAndPath("GET", ((GET) annotation).value(), false);
            else if (annotation instanceof HEAD)
                this.parseHttpMethodAndPath("HEAD", ((HEAD) annotation).value(), false);
            else if (annotation instanceof PATCH)
                this.parseHttpMethodAndPath("PATCH", ((PATCH) annotation).value(), true);
            else if (annotation instanceof POST)
                this.parseHttpMethodAndPath("POST", ((POST) annotation).value(), true);
            else if (annotation instanceof PUT) this.parseHttpMethodAndPath("PUT", ((PUT) annotation).value(), true);
            else if (annotation instanceof OPTIONS)
                this.parseHttpMethodAndPath("OPTIONS", ((OPTIONS) annotation).value(), false);
            else if (annotation instanceof BaseUrl) this.parseBaseUrl(((BaseUrl) annotation).value());
            else if (annotation instanceof HTTP) {
                HTTP http = (HTTP) annotation;
                this.parseHttpMethodAndPath(http.method(), http.path(), http.hasBody());
            } else if (annotation instanceof com.net.jianjia.http.Headers) {
                String[] headersToParse = ((com.net.jianjia.http.Headers) annotation).value();
                if (headersToParse.length == 0) throw methodError(this.method, "@Headers annotation is empty.");
                this.headers = this.parseHeaders(headersToParse);
            } else if (annotation instanceof Multipart) {
                if (this.isFormEncoded) throw methodError(this.method, "Only one encoding annotation is allowed.");
                this.isMultipart = true;
            } else if (annotation instanceof FormUrlEncoded) {
                if (this.isMultipart) throw methodError(this.method, "Only one encoding annotation is allowed.");
                this.isFormEncoded = true;
            }
        }

        /**
         * Parse base url.
         *
         * @param value the value
         */
        private void parseBaseUrl(String value) {
            HttpUrl baseUrl = HttpUrl.get(value);
            Utils.checkNotNull(baseUrl, "baseUrl == null");
            List<String> pathSegments = baseUrl.pathSegments();
            if (!"".equals(pathSegments.get(pathSegments.size() - 1)))
                throw new IllegalArgumentException("baseUrl must end in /: " + baseUrl);
            this.newBaseUrl = baseUrl;
        }

        /**
         * 解析请求头
         *
         * @param headers the headers
         * @return headers headers
         * @Headers("Cache-Control: max -age=640000")
         * @Headers({ "X-Foo: Bar", "X-Ping: Pong" })
         */
        private Headers parseHeaders(String[] headers) {
            Headers.Builder builder = new Headers.Builder();
            for (String header : headers) {
                // 查找冒号的位置
                int colon = header.indexOf(":");
                if (colon == -1 || colon == 0 || colon == header.length() - 1) throw methodError(this.method,
                        "@Headers value must be in the form \"Name: Value\". Found: \"%s\"", header);
                String headerName = header.substring(0, colon);
                String headerValue = header.substring(colon + 1).trim();
                if ("Content-Type".equalsIgnoreCase(headerName)) try {
                    this.contentType = MediaType.get(headerValue);
                } catch (IllegalArgumentException e) {
                    throw methodError(this.method, e, "Malformed content type: %s", headerValue);
                }
                else builder.add(headerName, headerValue);
            }
            return builder.build();
        }

        /**
         * 解析请求方法和路径
         *
         * @param httpMethod 请求方法
         * @param value      路径
         * @param hasBody    是否有请求头
         */
        private void parseHttpMethodAndPath(String httpMethod, String value, boolean hasBody) {
            if (this.httpMethod != null)
                throw methodError(this.method, "Only one HTTP method is allowed. Found: %s and %s.",
                        this.httpMethod, httpMethod);
            this.httpMethod = httpMethod;
            this.hasBody = hasBody;
            if (value.isEmpty()) return;
            // 不支持这种格式users?sortby={sortby}
            int question = value.indexOf("?");
            if (question != -1 && question < value.length() - 1) {
                String queryParams = value.substring(question + 1);
                Matcher queryParamMatcher = PARAM_URL_REGEX.matcher(queryParams);
                if (queryParamMatcher.find())
                    throw methodError(this.method, "URL query string \"%s\" must not have replace block. "
                            + "For dynamic query parameters use @Query.", queryParams);
            }
            this.relativeUrl = value;
            this.relativeUrlParamNames = parsePathParameters(value);
        }
    }
}
