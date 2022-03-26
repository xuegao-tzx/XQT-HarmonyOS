package com.xcl.location;

import com.xcl.location.Util.MyToastDialog;
import com.xcl.location.slice.MainAbilitySlice;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.Operation;
import ohos.agp.utils.LayoutAlignment;
import ohos.agp.window.service.WindowManager;
import ohos.bundle.IBundleManager;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.IntentConstants;
import ohos.utils.net.Uri;

/**
 * The type Main ability.
 */
public class MainAbility extends Ability {
    private static final HiLogLabel label = new HiLogLabel(HiLog.LOG_APP, 0x00234, "MainAbility");

    /**
     * The Main ability slice.
     */
    MainAbilitySlice MainAbilitySlice;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);

        super.setAbilitySliceAnimator(null);
        super.setTransitionAnimation(0, 0);
        this.getWindow().addFlags(WindowManager.LayoutConfig.MARK_ALLOW_EXTEND_LAYOUT);
        super.setMainRoute(MainAbilitySlice.class.getName());
        super.setAbilitySliceAnimator(null);
        super.setTransitionAnimation(0, 0);
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

    /**
     * 权限
     *
     * @param requestCode  the request code
     * @param permissions  the permissions
     * @param grantResults the grant results
     */
    @Override
    public void onRequestPermissionsFromUserResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsFromUserResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 20220109: {
                // 匹配requestPermissions的requestCode
                if (grantResults.length > 0
                        && grantResults[0] == IBundleManager.PERMISSION_GRANTED) {
                    // 权限被授予之后做相应业务逻辑的处理
                } else {
                    // 权限被拒绝
                    this.ShowDialog("权限被拒绝");
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
                return;
            }
        }
    }

    /**
     * Gets main ability slice.
     *
     * @return the main ability slice
     */
    public MainAbilitySlice getMainAbilitySlice() {
        return this.MainAbilitySlice;
    }

    /**
     * Sets main ability slice.
     *
     * @param bianQianShareAbilitySlice the bian qian share ability slice
     */
    public void setMainAbilitySlice(MainAbilitySlice bianQianShareAbilitySlice) {
        this.MainAbilitySlice = bianQianShareAbilitySlice;
    }
}
