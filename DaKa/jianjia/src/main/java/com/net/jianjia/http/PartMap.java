package com.net.jianjia.http;

/**
 * The interface Part map.
 *
 * @author 裴云飞
 * @date 2021 /4/26
 */
public @interface PartMap {

    /**
     * Encoding string.
     *
     * @return the string
     */
    String encoding() default "binary";
}
