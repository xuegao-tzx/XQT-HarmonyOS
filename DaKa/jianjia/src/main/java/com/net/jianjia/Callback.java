package com.net.jianjia;

/**
 * The interface Callback.
 *
 * @param <T> the type parameter
 * @modify&fix 田梓萱
 * @date 2022 /2/17
 */
public interface Callback<T> {

    /**
     * On response.
     *
     * @param call     the call
     * @param response the response
     */
    void onResponse(Call<T> call, Response<T> response);

    /**
     * On failure.
     *
     * @param call the call
     * @param t    the t
     */
    void onFailure(Call<T> call, Throwable t);
}
