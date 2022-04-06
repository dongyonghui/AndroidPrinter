package com.dyh.android.printer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dyh.android.printer.zxing.BarcodeFormat;
import com.dyh.android.printer.zxing.EncodeHintType;
import com.dyh.android.printer.zxing.MultiFormatWriter;
import com.dyh.android.printer.zxing.WriterException;
import com.dyh.android.printer.zxing.common.BitMatrix;
import com.dyh.android.printer.zxing.qrcode.QRCodeWriter;

import java.util.Hashtable;

/**
 * User: DongYonghui(404638723@qq.com)
 * Date: 2015-11-17
 * Time: 10:16
 */
public class ImageUtil {

    /**
     * 生成二维码 要转换的地址或字符串,可以是中文
     *
     * @param url
     * @param width
     * @param height
     * @return
     */
    public static Bitmap createQRImage(String url, final int width, final int height) {
        try {
            // 判断URL合法性
            if (url == null || "".equals(url) || url.length() < 1) {
                return null;
            }
            Hashtable<EncodeHintType, String> hints = new Hashtable<>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            // 图像数据转换，使用了矩阵转换
            BitMatrix bitMatrix = new QRCodeWriter().encode(url,
                    BarcodeFormat.QR_CODE, width, height, hints);
            int[] pixels = new int[width * height];
            // 下面这里按照二维码的算法，逐个生成二维码的图片，
            // 两个for循环是图片横列扫描的结果
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * width + x] = 0xff000000;
                    } else {
                        pixels[y * width + x] = 0xffffffff;
                    }
                }
            }
            // 生成二维码图片的格式，使用ARGB_8888
            Bitmap bitmap = Bitmap.createBitmap(width, height,
                    Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 生成条形码
     *
     * @param contents      需要生成的内容
     * @param desiredWidth  生成条形码的宽带
     * @param desiredHeight 生成条形码的高度
     * @return
     */
    public static Bitmap creatBarcode(String contents,
                                      int desiredWidth, int desiredHeight) {
        Bitmap ruseltBitmap = null;
        /**
         * 条形码的编码类型
         */
        BarcodeFormat barcodeFormat = BarcodeFormat.CODE_128;

        ruseltBitmap = encodeAsBitmap(contents, barcodeFormat,
                desiredWidth, desiredHeight);

        return ruseltBitmap;
    }

    /**
     * 生成条形码的Bitmap
     *
     * @param contents      需要生成的内容
     * @param format        编码格式
     * @param desiredWidth
     * @param desiredHeight
     * @return
     * @throws WriterException
     */
    protected static Bitmap encodeAsBitmap(String contents,
                                           BarcodeFormat format, int desiredWidth, int desiredHeight) {
        final int WHITE = 0xFFFFFFFF;
        final int BLACK = 0xFF000000;

        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix result = null;
        try {
            result = writer.encode(contents, format, desiredWidth,
                    desiredHeight, null);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        // All are 0, or black, by default
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    /**
     * 生成显示编码的Bitmap
     *
     * @param contents
     * @param width
     * @param height
     * @param context
     * @return
     */
    protected static Bitmap creatCodeBitmap(String contents, int width,
                                            int height, Context context) {
        TextView tv = new TextView(context);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tv.setLayoutParams(layoutParams);
        tv.setText(contents);
        tv.setHeight(height);
        tv.setGravity(Gravity.CENTER_HORIZONTAL);
        tv.setWidth(width);
        tv.setDrawingCacheEnabled(true);
        tv.setTextColor(Color.BLACK);
        tv.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        tv.layout(0, 0, tv.getMeasuredWidth(), tv.getMeasuredHeight());

        tv.buildDrawingCache();
        Bitmap bitmapCode = tv.getDrawingCache();
        return bitmapCode;
    }

    /**
     * 将两个Bitmap合并成一个
     *
     * @param first
     * @param second
     * @param fromPoint 第二个Bitmap开始绘制的起始位置（相对于第一个Bitmap）
     * @return
     */
    protected static Bitmap mixtureBitmap(Bitmap first, Bitmap second,
                                          PointF fromPoint) {
        if (first == null || second == null || fromPoint == null) {
            return null;
        }
        int marginW = 20;
        Bitmap newBitmap = Bitmap.createBitmap(
                first.getWidth() + second.getWidth() + marginW,
                first.getHeight() + second.getHeight(), Bitmap.Config.ARGB_4444);
        Canvas cv = new Canvas(newBitmap);
        cv.drawBitmap(first, marginW, 0, null);
        cv.drawBitmap(second, fromPoint.x, fromPoint.y, null);
        cv.restore();

        return newBitmap;
    }

    public static String getImagePath(long index) {
        String image;
        switch ((int) (index % 64)) {
            case 1:
                image = "http://www.world-expo.com.cn/img0/22858650644.23505454396.jpg";
                break;
            case 2:
                image = "http://img1.imgtn.bdimg.com/it/u=311487953,1101397406&fm=21&gp=0.jpg";
                break;
            case 3:
                image = "http://cdn.duitang.com/uploads/blog/201403/18/20140318120247_fud3d.thumb.300_0.jpeg";
                break;
            case 4:
                image = "http://img5.duitang.com/uploads/blog/201403/18/20140318115649_QjxJ8.jpeg";
                break;
            case 5:
                image = "http://img4.duitang.com/uploads/blog/201403/18/20140318115237_Y5Aik.jpeg";
                break;
            case 6:
                image = "http://cdn.duitang.com/uploads/blog/201403/18/20140318120254_LXV4P.thumb.300_0.jpeg";
                break;
            case 7:
                image = "http://pic14.nipic.com/20110522/7411759_164157418126_2.jpg";
                break;
            case 8:
                image = "http://pic2.ooopic.com/01/03/51/25b1OOOPIC19.jpg";
                break;
            case 9:
                image = "http://img2.3lian.com/img2007/19/33/005.jpg";
                break;
            case 10:
                image = "http://img.taopic.com/uploads/allimg/130501/240451-13050106450911.jpg";
                break;
            case 11:
                image = "http://pica.nipic.com/2008-03-19/2008319183523380_2.jpg";
                break;
            case 12:
                image = "http://pic.nipic.com/2007-11-09/200711912230489_2.jpg";
                break;
            case 13:
                image = "http://down.tutu001.com/d/file/20101129/2f5ca0f1c9b6d02ea87df74fcc_560.jpg";
                break;
            case 14:
                image = "http://zx.kaitao.cn/UserFiles/Image/beijingtupian6.jpg";
                break;
            case 15:
                image = "http://baike.soso.com/p/20090711/20090711101754-314944703.jpg";
                break;
            case 16:
                image = "http://imgsrc.baidu.com/forum/pic/item/3ac79f3df8dcd1004e9102b8728b4710b9122f1e.jpg";
                break;
            case 17:
                image = "http://www.photophoto.cn/m6/018/030/0180300271.jpg";
                break;
            case 18:
                image = "http://pica.nipic.com/2008-01-09/200819134250665_2.jpg";
                break;
            case 19:
                image = "http://anquanweb.com/uploads/userup/913/1322O9102-2596.jpg";
                break;
            case 20:
                image = "http://img.sucai.redocn.com/attachments/images/201012/20101213/20101211_0e830c2124ac3d92718fXrUdsYf49nDl.jpg";
                break;
            case 21:
                image = "http://pic28.nipic.com/20130401/9252150_195455485000_2.jpg";
                break;
            case 22:
                image = "http://h.hiphotos.baidu.com/zhidao/pic/item/0eb30f2442a7d9331794db0ead4bd11373f0018a.jpg";
                break;
            case 23:
                image = "http://pic.nipic.com/2007-11-08/2007118192311804_2.jpg";
                break;
            case 24:
                image = "http://www.th7.cn/Article/UploadFiles/200801/2008012120273536.jpg";
                break;
            case 25:
                image = "http://ppt360.com/background/UploadFiles_6733/201012/2010122016291897.jpg";
                break;
            case 26:
                image = "http://pic25.nipic.com/20121126/8305779_171431388000_2.jpg";
                break;
            case 27:
                image = "http://pic23.nipic.com/20120812/4277683_204018483000_2.jpg";
                break;
            case 28:
                image = "http://img2.3lian.com/img2007/10/28/123.jpg";
                break;
            case 29:
                image = "http://pic9.nipic.com/20100812/3289547_144304019987_2.jpg";
                break;
            case 30:
                image = "http://pic27.nipic.com/20130126/9252150_172332132344_2.jpg";
                break;
            case 31:
                image = "http://pic1.nipic.com/2009-02-19/200921922311483_2.jpg";
                break;
            case 32:
                image = "http://imgsrc.baidu.com/forum/pic/item/29618026cffc1e176e0d0bcc4a90f603718de9e2.jpg";
                break;
            case 33:
                image = "http://a2.att.hudong.com/38/59/300001054794129041591416974.jpg";
                break;
            case 34:
                image = "http://www.pptbz.com/pptpic/UploadFiles_6909/201111/20111115082649358.jpg";
                break;
            case 35:
                image = "http://pic15.nipic.com/20110624/7348760_084532494318_2.jpg";
                break;
            case 36:
                image = "http://www.pptbz.com/pptpic/UploadFiles_6909/201303/2013032913230597.jpg";
                break;
            case 37:
                image = "http://pic.nipic.com/2007-11-08/2007118192149610_2.jpg";
                break;
            case 38:
                image = "http://bj.ruideppt.com/upfile/proimage/201142918164560826.jpg";
                break;
            case 39:
                image = "http://pic.nipic.com/2008-01-05/200815191150944_2.jpg";
                break;
            case 40:
                image = "http://pic13.nipic.com/20110415/1347158_132411659346_2.jpg";
                break;
            case 41:
                image = "http://img.taopic.com/uploads/allimg/120210/2376-12021010060744.jpg";
                break;
            case 42:
                image = "http://www.pptbz.com/pptpic/UploadFiles_6909/201204/2012041411433867.jpg";
                break;
            case 43:
                image = "http://www.pptbz.com/Soft/UploadSoft/200911/2009110522430362.jpg";
                break;
            case 44:
                image = "http://www.photophoto.cn/m55/024/037/0240370130.jpg";
                break;
            case 45:
                image = "http://pic1.ooopic.com/uploadfilepic/sheji/2010-01-16/OOOPIC_1982zpwang407_201001165c068b555d204a85.jpg";
                break;
            case 46:
                image = "http://pic1.ooopic.com/uploadfilepic/sheji/2010-01-15/OOOPIC_1982zpwang407_2010011599c5bddc9477931d.jpg";
                break;
            case 47:
                image = "http://www.ppt123.net/beijing/uploadfiles_8374/201203/2012032517501327.jpg";
                break;
            case 48:
                image = "http://img.taopic.com/uploads/allimg/130529/240454-13052ZR31446.jpg";
                break;
            case 49:
                image = "http://xx.53shop.com/uploads/allimg/c090325/123O6031L40-10I1R.jpg";
                break;
            case 50:
                image = "http://www.ruideppt.net/upfile/proimage/201161713394918022.jpg";
                break;
            case 51:
                image = "http://pic1.ooopic.com/uploadfilepic/sheying/2010-01-16/OOOPIC_1982zpwang407_2010011604706ee1fd52b6e3.jpg";
                break;
            case 52:
                image = "http://pic1.nipic.com/2008-09-12/20089129255891_2.jpg";
                break;
            case 53:
                image = "http://pica.nipic.com/2007-11-16/20071116174041795_2.jpg";
                break;
            case 54:
                image = "http://img1.imgtn.bdimg.com/it/u=2920802548,3593517877&fm=21&gp=0.jpg";
                break;
            case 55:
                image = "http://img.taopic.com/uploads/allimg/120205/6517-1202051I34966.jpg";
                break;
            case 56:
                image = "http://pic1.ooopic.com/uploadfilepic/sheji/2010-01-15/OOOPIC_1982zpwang407_2010011582d1a0a490670dec.jpg";
                break;
            case 57:
                image = "http://img2.imgtn.bdimg.com/it/u=111578383,1112801653&fm=21&gp=0.jpg";
                break;
            case 58:
                image = "http://pic.nipic.com/2007-11-08/2007118192158381_2.jpg";
                break;
            case 59:
                image = "http://picm.photophoto.cn/073/073/052/0730520053.jpg";
                break;
            case 60:
                image = "http://pic1.ooopic.com/uploadfilepic/sheji/2009-10-13/OOOPIC_caihaiming_20091013352f72c70329b3d0.jpg";
                break;
            case 61:
                image = "http://pica.nipic.com/2007-12-22/200712229323215_2.jpg";
                break;
            case 62:
                image = "http://files.jb51.net/file_images/photoshop/201008/2010082021104513.jpg";
                break;
            case 63:
                image = "http://picm.photophoto.cn/012/077/018/0770180246.jpg";
                break;
            case 64:
                image = "http://pic23.nipic.com/20120831/4892276_203349544163_2.jpg";
                break;
            default:
                image = "http://pic.nipic.com/2007-11-09/2007119122519868_2.jpg";
                break;
        }
        return image;
    }
}
