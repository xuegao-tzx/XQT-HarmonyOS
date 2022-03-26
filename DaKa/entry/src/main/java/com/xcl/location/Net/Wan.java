package com.xcl.location.Net;

import com.net.jianjia.Call;
import com.net.jianjia.http.*;

import java.util.Map;

/**
 * The interface Wan.
 *
 * @author Xcl
 * @date 2022 /2/18
 * @package com.xcl.study.DataANet
 */
public interface Wan {
    /**
     * Gets dkxx.
     *
     * @param param the param
     * @return the dkxx
     */
    @BaseUrl("https://api.map.baidu.com/")
    @GET("/reverse_geocoding/v3")
    Call<DXJX> getDKXX(@QueryMap Map<String, String> param);

    /**
     * Post dkxx call.
     *
     * @param param the param
     * @return the call
     */
    @BaseUrl("https://yx.ty-ke.com/")
    @POST("/Home/Monitor/monitor_add")
    @FormUrlEncoded
    Call<TJXX> postDKXX(@FieldMap Map<String, String> param);
}
