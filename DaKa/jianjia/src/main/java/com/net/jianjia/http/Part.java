package com.net.jianjia.http;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 用于定义Multipart请求的每个part
 * <p>
 * 使用该注解定义的参数类型有以下3种方式可选:
 * <ul>
 * <li>如果类型是 {@link okhttp3.MultipartBody.Part} 内容将被直接使用。 省略part中的名称，即
 * ({@code @Part MultipartBody.Part part}).</li>
 * <li>如果类型是 {@link okhttp3.RequestBody RequestBody} 那么该值将直接与其内容类型一起使用。
 * 在注释中提供part名称 (例如,
 * {@code @Part("foo") RequestBody foo}).</li>
 * <li>其他对象类型将通过使用转换器转换为适当的格式。在注释中提供part名称 (例如,
 * {@code @Part("foo") Image photo}).</li>
 * </ul>
 * <p>
 * 使用该注解定义的参数，参数值可以为空，为空时，则忽略
 * <p>
 * <pre><code>
 * &#64;Multipart
 * &#64;POST("/")
 * Call&lt;ResponseBody&gt; example(
 *     &#64;Part("description") String description,
 *     &#64;Part(value = "image", encoding = "8-bit") RequestBody image);
 * </code></pre>
 * <p>
 *
 * @author 裴云飞
 * @date 2021 /1/18
 */
@Documented
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface Part {

    /**
     * Value string.
     *
     * @return the string
     */
    String value() default "";

    /**
     * 编码，默认为二进制
     *
     * @return string string
     */
    String encoding() default "binary";
}
