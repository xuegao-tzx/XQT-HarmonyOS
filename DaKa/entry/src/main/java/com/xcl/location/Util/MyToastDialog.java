package com.xcl.location.Util;

import com.xcl.location.Preference_RW;
import com.xcl.location.XLog;
import ohos.agp.components.AttrHelper;
import ohos.agp.components.ComponentContainer;
import ohos.agp.components.DirectionalLayout;
import ohos.agp.components.Text;
import ohos.agp.components.element.ShapeElement;
import ohos.agp.utils.Color;
import ohos.agp.utils.LayoutAlignment;
import ohos.agp.utils.TextAlignment;
import ohos.agp.window.dialog.ToastDialog;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

/**
 * The type My toast dialog.
 *
 * @author Xcl
 * @date 2021 /12/31
 * @package com.xcl.study.slice.Dialog
 */
public class MyToastDialog extends ToastDialog {
    /**
     * The constant label.
     */
    private static final HiLogLabel label = new HiLogLabel(HiLog.LOG_APP, 0x00234, "MyToastDialog");
    /**
     * The Text component.
     */
    private Text textComponent;

    /**
     * Instantiates a new My toast dialog.
     *
     * @param context the context
     * @param text    the text
     * @param style   the style
     * @param size    the size
     * @param pad     the pad
     */
    public MyToastDialog(Context context, String text, int style, int size, int pad) {
        super(context);
        this.init(context, style, size, pad);
        this.setText(text);
    }

    /**
     * Init.
     *
     * @param context the context
     * @param style   the style
     * @param size    the size
     * @param pad     the pad
     */
    private void init(Context context, int style, int size, int pad) {
        try {
            this.textComponent = new Text(context);
            this.textComponent.setPadding(pad, pad, pad, pad);
            this.textComponent.setTextColor(Color.BLACK);
            this.textComponent.setTextAlignment(TextAlignment.CENTER);
            this.textComponent.setTextSize(size);
            ShapeElement shapeElement = new ShapeElement(context, style);
            this.textComponent.setBackground(shapeElement);
            this.textComponent.setMultipleLine(true);
            DirectionalLayout.LayoutConfig layoutConfig = new DirectionalLayout.LayoutConfig();
            layoutConfig.width = ComponentContainer.LayoutConfig.MATCH_CONTENT;
            layoutConfig.height = ComponentContainer.LayoutConfig.MATCH_CONTENT;
            layoutConfig.alignment = LayoutAlignment.CENTER;
            this.textComponent.setLayoutConfig(layoutConfig);
            this.setTransparent(true);
            this.setComponent(this.textComponent);
            this.setCornerRadius(AttrHelper.vp2px(25, context));
        } catch (Exception e) {
            XLog.pd_pd = Preference_RW.ff7_r();
            XLog.error(label, e.getMessage());
        }
    }

    /**
     * Sets text.
     *
     * @param text the text
     * @return the text
     */
    @Override
    public MyToastDialog setText(String text) {
        this.textComponent.setText(text);
        return this;
    }
}
