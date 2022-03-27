package com.xcl.location.slice;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hms.location.harmony.*;
import com.net.jianjia.Call;
import com.net.jianjia.Callback;
import com.net.jianjia.Response;
import com.xcl.location.*;
import com.xcl.location.Net.DXJX;
import com.xcl.location.Net.TJXX;
import com.xcl.location.Util.MyToastDialog;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.Operation;
import ohos.agp.components.*;
import ohos.agp.utils.LayoutAlignment;
import ohos.app.Context;
import ohos.bundle.IBundleManager;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.IntentConstants;
import ohos.utils.net.Uri;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * The type Main ability slice.
 */
public class MainAbilitySlice extends AbilitySlice {
    private static final HiLogLabel label = new HiLogLabel(HiLog.LOG_APP, 0x00111, "MainAbilitySlice");
    /**
     * The Fused location client.
     */
    public FusedLocationClient fusedLocationClient;
    /**
     * The Settings provider client.
     */
    SettingsProviderClient settingsProviderClient;
    /**
     * The Location callback.
     */
    LocationCallback locationCallback;
    /**
     * The Location request.
     */
    LocationRequest locationRequest = new LocationRequest();
    /**
     * The Input.
     */
    TextField input;
    /**
     * The Inputtem.
     */
    TextField inputtem;
    /**
     * The Result.
     */
    Text result;
    /**
     * The Latitude.
     */
    Double latitude;
    /**
     * The Longitude.
     */
    Double Longitude;
    /**
     * The Province.
     */
    String province;
    /**
     * The City.
     */
    String city;
    /**
     * The District.
     */
    String district;
    /**
     * The Street.
     */
    String street;
    private Context context;
    private Text geoAddressInfoText;
    private Text locationInfoText;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_main_ability_slice);
        MainAbility bianQianShareAbility = (MainAbility) this.getAbility();
        bianQianShareAbility.setMainAbilitySlice(this);
        if (this.verifySelfPermission(ohos.security.SystemPermission.DISTRIBUTED_DATASYNC) != IBundleManager.PERMISSION_GRANTED) {
            if (this.canRequestPermission(ohos.security.SystemPermission.DISTRIBUTED_DATASYNC)) {
                this.requestPermissionsFromUser(
                        new String[]{ohos.security.SystemPermission.LOCATION}, 20220109);
            } else {
                // 显示应用需要权限的理由，提示用户进入设置授权
                this.ShowDialog("请在设置中开启定位权限");
                /*无参--页面跳转开始*/
                Intent intent1 = new Intent();
                Operation operation = new Intent.OperationBuilder()
                        .withAction(IntentConstants.ACTION_APPLICATION_DETAILS_SETTINGS)
                        .withUri(Uri.getUriFromParts("package", getBundleName(), null))
                        .build();
                intent1.setOperation(operation);
                startAbility(intent1);
                this.onBackPressed();
            }
        }
        locationInfoText = (Text) findComponentById(ResourceTable.Id_location_info);
        geoAddressInfoText = (Text) findComponentById(ResourceTable.Id_geo_address_info);
        input = (TextField) findComponentById(ResourceTable.Id_dxzh);
        inputtem = (TextField) findComponentById(ResourceTable.Id_dxtem);
        result = (Text) findComponentById(ResourceTable.Id_dkjg);
        inputtem.setEnabled(false);
        if (Preference_RW.ff1_r() == null || Preference_RW.ff1_r().equals("") || Preference_RW.ff1_r().equals("0")) {
            input.setText("");
        } else {
            input.setText(Preference_RW.ff1_r());
        }
        if (Preference_RW.ff2_r() == 2) {
            if (Preference_RW.ff3_r() == null || Preference_RW.ff3_r().equals("") || Preference_RW.ff3_r().equals("0")) {
                inputtem.setText("");
            } else {
                inputtem.setText(Preference_RW.ff3_r());
            }
        }
        XLog.info(label, "onStart start");
        fusedLocationClient = new FusedLocationClient(this);
        settingsProviderClient = new SettingsProviderClient(this);
// 设置位置更新的间隔（毫秒：单位）
        locationRequest.setInterval(5000);
// 设置权重
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
// （可选）设置是否需要返回地址信息
        locationRequest.setNeedAddress(true);
// （可选）设置返回地址信息的语言
        locationRequest.setLanguage("zh");
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    // 处理位置回调结果
                    XLog.info(label, "onLocationResult");
                    latitude = locationResult.getLastHWLocation().getLatitude();
                    Longitude = locationResult.getLastHWLocation().getLongitude();
                }
            }

            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {
                super.onLocationAvailability(locationAvailability);
                if (locationAvailability != null) {
                    // 处理位置状态
                    XLog.info(label, "onLocationAvailability");

                }
            }
        };
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback)
                .addOnSuccessListener(new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object v) {
                        // 接口调用成功的处理
                        XLog.info(label, "requestLocationUpdates success");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        // 接口调用失败的处理
                        XLog.error(label, "requestLocationUpdates failure");
                    }
                });
        context = this;
        Button stopLocatingButton = (Button) findComponentById(ResourceTable.Id_stop_locating);
        Map<String, String> url = new HashMap<String, String>();
        Map<String, String> url1 = new HashMap<String, String>();
        Map<String, String> url2 = new HashMap<String, String>();
        url.put("ak", "此处填写你的百度地图ak");//TODO:百度地图ak
        url.put("output", "json");
        url.put("coordtype", "wgs84ll");
        stopLocatingButton.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {
                // 注意：停止位置更新时，mLocationCallback与requestLocationUpdates()中的LocationCallback参数为同一对象。
                fusedLocationClient.removeLocationUpdates(locationCallback)
                        .addOnSuccessListener(v -> {
                            // 接口调用成功的处理
                            XLog.info(label, "removeLocationUpdates success");
                        })
                        .addOnFailureListener(e -> {
                            // 接口调用失败的处理
                            XLog.error(label, "removeLocationUpdates failure");
                        });
                locationInfoText.setText(latitude + "," + Longitude);
                XLog.error(label, latitude + "," + Longitude);
                url.put("location", latitude + "," + Longitude);
                MyApplication.getInstance().getWan().getDKXX(url).enqueue(new Callback<DXJX>() {

                    @Override
                    public void onResponse(Call<DXJX> call, Response<DXJX> response) {
                        province = response.body().result.addressComponent.province;
                        city = response.body().result.addressComponent.city;
                        district = response.body().result.addressComponent.district;
                        street = response.body().result.addressComponent.street;
                        geoAddressInfoText.setText(response.body().result.addressComponent.province +
                                response.body().result.addressComponent.city +
                                response.body().result.addressComponent.district +
                                response.body().result.addressComponent.street);
                        XLog.error(label, response.body().result.addressComponent.province +
                                response.body().result.addressComponent.city +
                                response.body().result.addressComponent.district +
                                response.body().result.addressComponent.street);
                    }

                    @Override
                    public void onFailure(Call<DXJX> call, Throwable t) {
                        XLog.error(label, t.getMessage());
                    }
                });
            }
        });
        Button save = (Button) findComponentById(ResourceTable.Id_save_button);
        save.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {
                Preference_RW.ff1_w(input.getText());
                if (Preference_RW.ff2_r() == 2) {
                    Preference_RW.ff3_w(inputtem.getText());
                }
                ShowDialog("保存成功!");
            }
        });
        RadioContainer radioContainer = (RadioContainer) findComponentById(ResourceTable.Id_radio_container0);
        radioContainer.cancelMarks();
        if (Preference_RW.ff2_r() == 1) {
            radioContainer.mark(0);
            Random rand = new Random();//36.0-36.9
            DecimalFormat df = new DecimalFormat("0.0");
            inputtem.setText(df.format((float) (rand.nextInt(10) + 360) / 10));
        } else if (Preference_RW.ff2_r() == 2) {
            radioContainer.mark(1);
            inputtem.setText("");
            inputtem.setEnabled(true);
        } else {
            radioContainer.mark(0);
            Preference_RW.ff2_w(1);
            Random rand = new Random();//36.0-36.9
            DecimalFormat df = new DecimalFormat("0.0");
            inputtem.setText(df.format((float) (rand.nextInt(10) + 360) / 10));
        }
        radioContainer.setMarkChangedListener(new RadioContainer.CheckedStateChangedListener() {
            @Override
            public void onCheckedChanged(RadioContainer radioContainer, int i) {
                XLog.info(label, "用户选择的是:" + i);
                if (i == 0) {
                    XLog.info(label, "用户选择的是:是");
                    Preference_RW.ff2_w(1);
                    Random rand = new Random();//36.0-36.9
                    DecimalFormat df = new DecimalFormat("0.0");
                    inputtem.setText(df.format((float) (rand.nextInt(10) + 360) / 10));
                    inputtem.setEnabled(false);
                } else if (i == 1) {
                    XLog.info(label, "用户选择的是:否");
                    Preference_RW.ff2_w(2);
                    inputtem.setText("");
                    inputtem.setEnabled(true);
                } else {
                    XLog.error(label, "这tm不可能！");
                }
            }
        });
        RadioContainer radioContainer1 = (RadioContainer) findComponentById(ResourceTable.Id_radio_container1);
        radioContainer1.cancelMarks();
        if (Preference_RW.ff4_r() == 1) {
            radioContainer1.mark(0);
        } else if (Preference_RW.ff4_r() == 2) {
            radioContainer1.mark(1);
        } else {
            radioContainer1.mark(0);
            Preference_RW.ff4_w(1);
        }
        radioContainer1.setMarkChangedListener(new RadioContainer.CheckedStateChangedListener() {
            @Override
            public void onCheckedChanged(RadioContainer radioContainer, int i) {
                XLog.info(label, "用户选择的是:" + i);
                if (i == 0) {
                    XLog.info(label, "用户选择的是:是");
                    Preference_RW.ff4_w(1);
                } else if (i == 1) {
                    XLog.info(label, "用户选择的是:否");
                    Preference_RW.ff4_w(2);
                } else {
                    XLog.error(label, "这tm不可能！");
                }
            }
        });
        Button daka = (Button) findComponentById(ResourceTable.Id_sddk_button);
        daka.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {
                if(Preference_RW.ff1_r().equals("")||Preference_RW.ff1_r()==null||Preference_RW.ff1_r().equals("0")){
                    Preference_RW.ff1_w(input.getText());
                }else{}
                url1.put("mobile", Preference_RW.ff1_r());
                url1.put("city", city);
                url1.put("jk_type", "健康");
                url1.put("district", district);
                url1.put("address", province + city + district + street);
                url1.put("title", inputtem.getText());
                url1.put("jc_type", "否");
                url1.put("wc_type", "否");
                url1.put("is_verify", "0");
                url1.put("province", province);
                url2.put("mobile", Preference_RW.ff1_r());
                url2.put("days", "null");
                url2.put("daka_status", "null");
                MyApplication.getInstance().getWan().postCXDKXX(url2).enqueue(new Callback<TJXX>(){
                    @Override
                    public void onResponse(Call<TJXX> call, Response<TJXX> response) {

                        if (response.isSuccessful()) {
                            if(response.body().code.equals("200")){
                                ShowDialog("当前时间段您已经打过卡了...");
                                result.setText("当前时间段您已经完成打卡!");
                            }else if(response.body().code.equals("404")){
                                MyApplication.getInstance().getWan().postDKXX(url1).enqueue(new Callback<TJXX>() {
                                    @Override
                                    public void onResponse(Call<TJXX> call, Response<TJXX> response) {
                                        if (response.isSuccessful()) {
                                            ShowDialog("打卡成功!");
                                            XLog.info(label, response.body().msg);
                                            result.setText(response.body().msg);
                                        }else{
                                            ShowDialog("打卡失败，请稍后重试...");
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<TJXX> call, Throwable t) {
                                        XLog.error(label, t.getMessage());
                                        ShowDialog("打卡失败!错误原因:" + t.getMessage());
                                    }
                                });
                            }
                        }else{
                            XLog.error(label, "访问失败！");
                        }
                    }

                    @Override
                    public void onFailure(Call<TJXX> call, Throwable t) {
                        XLog.error(label, t.getMessage());
                        ShowDialog("查询失败!错误原因:" + t.getMessage());
                    }
                });
            }
        });
    }

    private void ShowDialog(String text1) {
        try {
            new MyToastDialog(this.getContext(), text1, ResourceTable.Graphic_xtoast_framem, 36, 25)
                    .setDuration(120)
                    .setAlignment(LayoutAlignment.BOTTOM)
                    .setOffset(0, 100)
                    .show();
        } catch (Exception e) {

            XLog.error(label, e.getMessage());
        }
    }

    @Override
    protected void onStop() {
        XLog.info(label, "onStop start");
        super.onStop();
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
                    .addOnSuccessListener(v -> {
                        // 接口调用成功的处理
                        XLog.info(label, "removeLocationUpdates success");
                    })
                    .addOnFailureListener(e -> {
                        // 接口调用失败的处理
                        XLog.error(label, "removeLocationUpdates failure");
                    });
        } catch (Exception e) {

            XLog.error(label, e.getMessage());
        }
        XLog.info(label, "onStop end");
    }
}

