package com.dyh.android.printer;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.dyh.android.printer.parser.EscCommand;
import com.dyh.android.printer.parser.FormatTempleteParser;
import com.dyh.android.printer.ui.PrinterDialogHelper;
import com.dyh.android.printer.velocity.PrinterBean;
import com.dyh.android.printer.velocity.RowTool;
import com.dyh.android.printer.velocity.VelocityContentRender;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Locale;
import java.util.Vector;

/**
 * User: DongYonghui(404638723@qq.com)
 * Date: 2015-12-26
 * Time: 15:05
 * 打印机工具类
 */
public class UsbPrintManager {
    public static final byte CUT_PAPER_AND_FEED = 20;

    //默认的事件处理监听
    private final OnPrinterNotifyListener mDefaultNotifyListener = new OnPrinterNotifyListener() {
        @Override
        public void onPrinterNotify(final NotifyMessage notifyMessage) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                handMessageOnUIThread(notifyMessage);
            } else {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        handMessageOnUIThread(notifyMessage);
                    }
                });
            }
        }

        private void handMessageOnUIThread(final NotifyMessage notifyMessage) {
            //弹框处理
            if (isNeedShowPrintingDialog && null != mActivity) {
                //打印失败则隐藏对话框
                if (notifyMessage.getCode() >= 0x1100 && notifyMessage.getCode() < 0x1200) {
                    PrinterDialogHelper.showDialog(mActivity, mActivity.getString(R.string.printer_printFailed), notifyMessage.getInfo());
                } else {//打印开始弹框提示，结束关闭提示
                    switch (notifyMessage) {
                        case USB_PRINTER_CONNECTED://设备连接成功后打印数据
                            print(mContext, mPrinterBean);
                            break;
                        case PRINT_START:
                            PrinterDialogHelper.showDialog(mActivity, mActivity.getString(R.string.printer_printing), notifyMessage.getInfo());
                            break;
                        case PRINT_FINISH:
                            PrinterDialogHelper.hideDialog();
                            break;
                    }
                }

            }

            if (null != onPrinterNotifyListener) {
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    onPrinterNotifyListener.onPrinterNotify(notifyMessage);
                } else {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            onPrinterNotifyListener.onPrinterNotify(notifyMessage);
                        }
                    });
                }
            }
        }
    };//打印监听器
    private OnPrinterNotifyListener onPrinterNotifyListener;//打印监听器
    private boolean isNeedShowPrintingDialog = true;//是否需要在打印过程中弹框处理
    private Activity mActivity;
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    private static final VelocityContentRender render = new VelocityContentRender();
    private USBPrinter mUsbPrinter;
    private static UsbPrintManager instance;

    private PrinterBean mPrinterBean;//打印数据
    private Context mContext;

    private UsbPrintManager() {
        mUsbPrinter = USBPrinter.getInstance();
    }

    public static UsbPrintManager getInstance() {
        synchronized (UsbPrintManager.class) {
            if (null == instance) {
                instance = new UsbPrintManager();
            }
        }
        return instance;
    }


    public OnPrinterNotifyListener getOnPrinterNotifyListener() {
        return onPrinterNotifyListener;
    }

    /**
     * 设置通知回调监听器
     *
     * @param onPrinterNotifyListener 需要监听的监听器对象
     * @return
     */
    public UsbPrintManager setOnPrinterNotifyListener(OnPrinterNotifyListener onPrinterNotifyListener) {
        this.onPrinterNotifyListener = onPrinterNotifyListener;
        return instance;
    }

    /**
     * 打印消息
     *
     * @param context
     * @param printerBean 获取方法见demo和 PrinterBean.getPrinterBean()方法
     * @return
     */
    public void print(final Context context, final PrinterBean printerBean) {
        this.mContext = context;
        this.mPrinterBean = printerBean;

        if (null == printerBean || null == context) {
            sendNotify(OnPrinterNotifyListener.NotifyMessage.PRINT_FAILED_PARAMS_ERROR);
            return;
        }

        //如果没有连接打印机，则进行连接设备
        if (!mUsbPrinter.isConnected()) {
            sendNotify(OnPrinterNotifyListener.NotifyMessage.WAITING_CONNECT_DEVICE);
            mUsbPrinter.connect(context);
            return;
        }

        new Thread() {
            @Override
            public void run() {
                super.run();
                HashMap<String, Object> contentMap = new HashMap<>();
                contentMap.put(printerBean.templateBeanKey, printerBean.templateBean);
                contentMap.put("row", new RowTool());
                contentMap.put("number", new MyNumberTool());
                Locale defaultLocal = Locale.getDefault();
                Locale.setDefault(Locale.ENGLISH);
                String printInfo = render.render(printerBean.templateInfo, contentMap);
                printInfo = printInfo.replace("¤", "￥");
                Locale.setDefault(defaultLocal);


                if (TextUtils.isEmpty(printInfo) || printerBean.printCount <= 0) {
                    sendNotify(OnPrinterNotifyListener.NotifyMessage.PRINT_FINISH);
                    return;
                }
                sendCommend(context, getEscCommand(printInfo, printerBean.printCount, true));
            }
        }.start();
    }


    private static EscCommand getEscCommand(final String printInfo, final int printCount, boolean isNeedCutPaper) {
        EscCommand esc = new EscCommand();
//        设置语言环境
//            Vector<Byte> vector = new Vector<>();
//            byte[] command = new byte[]{0x1B, 0x23, 0x23, 0x53, 0x4C, 0x41, 0x4E, 0x0f};
//            for (int i = 0; i < command.length; i++) {
//                vector.add(command[i]);
//            }
//            esc.getCommand().addAll(vector);

        //清空回车符
        for (int i = 0; i < printCount; i++) {
            FormatTempleteParser.addEscCommand(printInfo, esc);

            byte feedLine = 2;
            esc.addPrintAndFeedLines(feedLine);
            if (isNeedCutPaper) {
                esc.addCutPaperAndFeed(CUT_PAPER_AND_FEED);
            } else {
                esc.addCutPaper();
                esc.addPrintAndFeedLines(feedLine);
            }
        }
        return esc;
    }

    private void sendCommend(final Context context, final EscCommand esc) {
        Vector<Byte> command = esc.getCommand();
        int length = esc.getCommand().size();
        byte[] commend = new byte[length];
        for (int i = 0; i < length; i++) {
            commend[i] = command.get(i);
        }

        //如果打开失败，重试10次
        int retryOpenCount = 0;
        sendNotify(OnPrinterNotifyListener.NotifyMessage.WAITING_CONNECT_DEVICE);
        while (!mUsbPrinter.isConnected()) {
            if (retryOpenCount++ > 10) {
                break;
            }
            mUsbPrinter.initPrinter(context);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (mUsbPrinter.isConnected()) {
            mUsbPrinter.write(commend);
        } else {
            sendNotify(OnPrinterNotifyListener.NotifyMessage.PRINT_FAILED_DEVICE_CANT_CONNECT);
        }
    }


    /**
     * 发送通知给使用者
     *
     * @param msg
     */
    public UsbPrintManager sendNotify(final OnPrinterNotifyListener.NotifyMessage msg) {
        mDefaultNotifyListener.onPrinterNotify(msg);
        return instance;
    }

    /**
     * 是否usb打印机资源
     */
    public void releasePrinter() {
        mUsbPrinter.close();
    }

    public boolean isNeedShowPrintingDialog() {
        return isNeedShowPrintingDialog;
    }

    /**
     * 是否需要弹框提示正在打印
     *
     * @param activity
     * @param needShowPrintingDialog
     * @return
     */
    public UsbPrintManager setNeedShowPrintingDialog(Activity activity, boolean needShowPrintingDialog) {
        this.mActivity = activity;
        this.isNeedShowPrintingDialog = needShowPrintingDialog;
        return instance;
    }
}
