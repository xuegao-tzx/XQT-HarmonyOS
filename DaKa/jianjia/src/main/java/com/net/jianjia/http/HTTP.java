package com.net.jianjia.http;

import java.lang.annotation.*;

/**
 * 用于发送一个自定义的HTTP请求，声明在方法上的注解，在运行时有效
 * <pre><code>
 * interface Service {
 *   &#064;HTTP(method = "CUSTOM", path = "custom/endpoint/")
 *   Call&lt;ResponseBody&gt; customEndpoint();
 * }
 * </code></pre>
 * 发送一个{@code DELETE}请求
 * <pre><code>
 * interface Service {
 *   &#064;HTTP(method = "DELETE", path = "remove/", hasBody = true)
 *   Call&lt;ResponseBody&gt; deleteObject(@Body RequestBody object);
 * }
 * </code></pre>
 *
 * @author 裴云飞
 * @date 2021 /1/18
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface HTTP {

    /**
     * Method string.
     *
     * @return the string
     */
    String method();

    /**
     * Has body boolean.
     *
     * @return the boolean
     */
    boolean hasBody() default false;

    /**
     * PUT注解一般必须添加相对路径或绝对路径或者全路径，如果不想在PUT注解后添加请求路径，
     * 则可以在方法的第一个参数中用{@link com.net.jianjia.http.Url @Url}注解添加请求路径
     *
     * @return string string
     */
    String path() default "";
}
