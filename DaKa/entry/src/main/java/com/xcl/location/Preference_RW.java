package com.xcl.location;

import ohos.aafwk.ability.Ability;
import ohos.app.Context;
import ohos.data.DatabaseHelper;
import ohos.data.preferences.Preferences;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

/**
 * The type Preference rw.
 *
 * @author Xcl
 * @date 2022 /3/25
 * @package com.xcl.location
 */
public class Preference_RW extends Ability {
    private static final HiLogLabel label = new HiLogLabel(HiLog.LOG_APP, 0x00234, "Preference_RW");
    private static final String preferenceFile = "messages_tzx_xcl_dx";
    private static final String counterKey7 = "DeBug";
    private static final String counterKey1 = "dszh";
    private static final String counterKey2 = "dxtempd";
    private static final String counterKey3 = "dxtem";
    private static final String counterKey4 = "autodxpd";
    /**
     * The constant context.
     */
    public static Context context;

    private static void writeCounter(String key, int message) {
        try {
            DatabaseHelper databaseHelper = new DatabaseHelper(context);
            Preferences preferences = databaseHelper.getPreferences(preferenceFile);
            preferences.putInt(key, message);
            preferences.flush();
        } catch (Exception e) {
            HiLog.error(label, "写发生错误[" + e.getMessage() + "],key=[" + key + "],message=[" + message + "]");
        }
    }

    private static void writeCounter(String key, long message) {
        try {
            DatabaseHelper databaseHelper = new DatabaseHelper(context);
            Preferences preferences = databaseHelper.getPreferences(preferenceFile);
            preferences.putLong(key, message);
            preferences.flush();
        } catch (Exception e) {
            HiLog.error(label, "写发生错误[" + e.getMessage() + "],key=[" + key + "],message=[" + message + "]");
        }
    }

    private static void writeCounter(String key, String message) {
        try {
            DatabaseHelper databaseHelper = new DatabaseHelper(context);
            Preferences preferences = databaseHelper.getPreferences(preferenceFile);
            preferences.putString(key, message);
            preferences.flush();
        } catch (Exception e) {
            HiLog.warn(label, "xcl写发生错误[" + e.getMessage() + "],key=[" + key + "],message=[" + message + "]");
        }
    }

    private static int readCounter(String key) {
        try {
            DatabaseHelper databaseHelper = new DatabaseHelper(context);
            Preferences preferences = databaseHelper.getPreferences(preferenceFile);
            return preferences.getInt(key, 0);
        } catch (Exception e) {
            HiLog.error(label, "读发生错误" + e.getMessage());
            return 0;
        }
    }

    private static long readCounter(String key, int a) {
        try {
            XLog.info(label, "没必要的数据:" + a);
            DatabaseHelper databaseHelper = new DatabaseHelper(context);
            Preferences preferences = databaseHelper.getPreferences(preferenceFile);
            return preferences.getLong(key, 0);
        } catch (Exception e) {
            HiLog.error(label, "读发生错误" + e.getMessage());
            return 0;
        }
    }

    private static String readCounter(String key, String a) {
        try {
            XLog.info(label, "没必要的数据:" + a);
            DatabaseHelper databaseHelper = new DatabaseHelper(context);
            Preferences preferences = databaseHelper.getPreferences(preferenceFile);
            return preferences.getString(key, String.valueOf(0));
        } catch (Exception e) {
            HiLog.warn(label, "xcl读发生错误" + e.getMessage());
            return "0";
        }
    }

    /**
     * Ff 7 r int.
     *
     * @return the int
     */
    public static int ff7_r() {
        return readCounter(counterKey7);
    }

    /**
     * Ff 7 w.
     *
     * @param a1 the a 1
     */
    public static void ff7_w(int a1) {
        Preference_RW.writeCounter(counterKey7, a1);
    }

    /**
     * Ff 2 r int.
     *
     * @return the int
     */
    public static int ff2_r() {
        return readCounter(counterKey2);
    }

    /**
     * Ff 2 w.
     *
     * @param a1 the a 1
     */
    public static void ff2_w(int a1) {
        Preference_RW.writeCounter(counterKey2, a1);
    }

    /**
     * Ff 4 r int.
     *
     * @return the int
     */
    public static int ff4_r() {
        return readCounter(counterKey4);
    }

    /**
     * Ff 4 w.
     *
     * @param a1 the a 1
     */
    public static void ff4_w(int a1) {
        Preference_RW.writeCounter(counterKey4, a1);
    }

    /**
     * Ff 1 r string.
     *
     * @return the string
     */
    public static String ff1_r() {
        return readCounter(counterKey1, "0");
    }

    /**
     * Ff 1 w.
     *
     * @param a1 the a 1
     */
    public static void ff1_w(String a1) {
        Preference_RW.writeCounter(counterKey1, a1);
    }

    /**
     * Ff 3 r string.
     *
     * @return the string
     */
    public static String ff3_r() {
        return readCounter(counterKey3, "0");
    }

    /**
     * Ff 3 w.
     *
     * @param a1 the a 1
     */
    public static void ff3_w(String a1) {
        Preference_RW.writeCounter(counterKey3, a1);
    }
}
