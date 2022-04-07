package com.dyh.android.printer.velocity;

import android.content.Context;

import com.dyh.android.printer.TxtReader;

import java.io.Serializable;

/**
 * User: DongYonghui(404638723@qq.com)
 * Date: 2016-01-03
 * Time: 20:31
 * 打印小票
 */
public class PrinterBean implements Serializable {
    /**
     * 打印多少张小票，默认打印一张
     */
    public int printCount = 1;
    /**
     * 小票模板信息(velocity模板)
     */
    public String templateInfo;
    /**
     * 小票模板velocity中一级Bean的key
     */
    public String templateBeanKey;

    /**
     * 小票模板velocity中一级Bean
     */
    public Object templateBean;

    /**
     * 生成小票信息数据
     *
     * @param context               上下文
     * @param assesTemplateFileName assess文件夹中小票模板文件名
     * @param templateRootKeyName   小票模板中自定义数据key
     * @param data                  展示的数据
     * @return
     */
    public static PrinterBean getPrinterBean(Context context, String assesTemplateFileName, String templateRootKeyName, Object data) {
        return getPrinterBean(context, assesTemplateFileName, templateRootKeyName, data, 1);
    }

    /**
     * 生成小票信息数据
     *
     * @param context               上下文
     * @param assesTemplateFileName assess文件夹中小票模板文件名
     * @param templateRootKeyName   小票模板中自定义数据key
     * @param data                  展示的数据
     * @param count                 打印数量
     * @return
     */
    public static PrinterBean getPrinterBean(Context context, String assesTemplateFileName, String templateRootKeyName, Object data, int count) {
        PrinterBean printerBean = null;
        if (null != data) {
            printerBean = new PrinterBean();
            printerBean.templateInfo = TxtReader.getStringFromAssetsByFullPath(context, assesTemplateFileName);
            printerBean.printCount = count;
            printerBean.templateBeanKey = templateRootKeyName;
            printerBean.templateBean = data;
        }
        return printerBean;
    }
}
