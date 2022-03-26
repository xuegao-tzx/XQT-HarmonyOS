package com.xcl.location;

import ohos.aafwk.ability.Ability;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

/**
 * The type X log.
 *
 * @author Xcl
 * @date 2022 /1/24
 * @package com.xcl.study.Log
 */
public class XLog extends Ability {
    /**
     * The constant pd_pd.
     */
    public static int pd_pd;
    /**
     * The .
     */
    static int i = 0;//TODO:其实这个值P用没有，只是因为有些情况下不输出日志就会卡死≥▂≤

    /**
     * Info.
     *
     * @param label   the label
     * @param message the message
     */
    public static void info(HiLogLabel label, String message) {
        if (pd_pd == 555) {
            HiLog.info(label, "信息:[" + message + "]");
        } else {
            i++;
        }
        //HiLog.info(label, "信息:[" + message + "]");
    }

    /**
     * Warn.
     *
     * @param label   the label
     * @param message the message
     */
    public static void warn(HiLogLabel label, String message) {
        if (pd_pd == 555) {
            HiLog.warn(label, "警告:[" + message + "]");
        } else {
            i++;
        }
        //HiLog.warn(label, "警告:[" + message + "]");
    }

    /**
     * Error.
     *
     * @param label   the label
     * @param message the message
     */
    public static void error(HiLogLabel label, String message) {
        if (pd_pd == 555) {
            HiLog.error(label, "错误:[" + message + "]");
        } else {
            i++;
        }
        //HiLog.error(label, "错误:[" + message + "]");
    }
}
