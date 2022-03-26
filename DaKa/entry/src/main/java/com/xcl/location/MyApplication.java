package com.xcl.location;

import com.huawei.hms.analytics.HiAnalyticsTools;
import com.net.jianjia.JianJia;
import com.net.jianjia.gson.GsonConverterFactory;
import com.xcl.location.Net.Wan;
import ohos.aafwk.ability.AbilityPackage;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * The type My application.
 */
public class MyApplication extends AbilityPackage {
    private static final HiLogLabel label = new HiLogLabel(HiLog.LOG_APP, 0x00234, "MyApplication");
    private static MyApplication Instance;
    private JianJia mJianJia;
    private Wan mWan;

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static MyApplication getInstance() {
        return Instance;
    }

    /**
     * Gets jian jia.
     *
     * @return the jian jia
     */
    public JianJia getJianJia() {
        return this.mJianJia;
    }

    /**
     * Gets wan.
     *
     * @return the wan
     */
    public Wan getWan() {
        return this.mWan;
    }

    @Override
    public void onInitialize() {
        super.onInitialize();
        try {
            Preference_RW.context = this.getContext();
            XLog.pd_pd = Preference_RW.ff7_r();
            XLog.pd_pd = 555;
            HiAnalyticsTools.enableLog();
            //创建日志拦截器
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            //为OKHTTP添加日志拦截器
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();
            this.mJianJia = new JianJia.Builder()
                    // 使用自定义的okHttpClient对象
                    .callFactory(okHttpClient)
                    .baseUrl("https://api.xuegao-xcl.tech")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            Instance = this;
            this.mWan = this.mJianJia.create(Wan.class);
        } catch (Exception e) {
            XLog.error(label, e.getMessage());
        }
    }
}
