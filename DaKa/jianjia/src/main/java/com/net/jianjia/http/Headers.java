package com.net.jianjia.http;

/**
 * 用于添加一个或多个请求头
 * <pre><code>
 * &#64;Headers("Cache-Control: max-age=640000")
 * &#64;GET("/")
 * ...
 *
 * &#64;Headers({
 *   "X-Foo: Bar",
 *   "X-Ping: Pong"
 * })
 * &#64;GET("/")
 * ...
 * </code></pre>
 * <strong>注意：</strong> 具有相同名称的请求头不会相互覆盖,而是会照样添加到请求头中
 *
 * @author 裴云飞
 * @date 2021 /1/18
 */
public @interface Headers {

    /**
     * Value string [ ].
     *
     * @return the string [ ]
     */
    String[] value();
}
