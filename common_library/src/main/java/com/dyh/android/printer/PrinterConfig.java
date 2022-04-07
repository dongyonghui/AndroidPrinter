package com.dyh.android.printer;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

/**
 * 作者：DongYonghui
 * 邮箱：648731994@qq.com
 * 创建时间：2019/12/2/002 11:04
 * 描述：打印机配置信息
 */
public class PrinterConfig {
    private static final String FILENAME = "PrinterConfig";
    private static final String PRINTER_CONFIG = "printer_config";//打印机配置信息

    private int printCount = 1;//每联打印的数量
    private int pagerWidth = 58;//小票宽度（58：80）

    /**
     * 获取打印机配置信息
     *
     * @param context
     * @return
     */
    public static PrinterConfig getPrinterConfig(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(FILENAME, Context.MODE_PRIVATE);
        String json = sharedPreferences.getString(PRINTER_CONFIG, null);
        PrinterConfig printerConfig = new Gson().fromJson(json, PrinterConfig.class);
        if (printerConfig == null) {
            printerConfig = new PrinterConfig();
        }
        return printerConfig;
    }


    /**
     * 保存打印机配置信息
     *
     * @param context
     * @param printerConfig
     * @return
     */
    public static void saveConfigInfo(Context context, PrinterConfig printerConfig) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(FILENAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PRINTER_CONFIG, new Gson().toJson(printerConfig));
        editor.apply();
    }

    public int getPrintCount() {
        return printCount;
    }

    public void setPrintCount(int printCount) {
        this.printCount = printCount;
    }

    public int getPagerWidth() {
        return pagerWidth;
    }

    public void setPagerWidth(int pagerWidth) {
        this.pagerWidth = pagerWidth;
    }
}
