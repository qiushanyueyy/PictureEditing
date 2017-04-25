package com.yangy.pictureediting;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by yangy on 2017/04/20 09:42
 */

public class BitmapUtils {

    /**
     * 通过图片路径获取bitmap对象（支持本地图片路径和网络图片路径）
     *
     * @param graphPath 图片路径
     * @return 图片的bitmap对象
     */
    public static Bitmap getBitmap(String graphPath) {
        Bitmap bitmapNet;
        File file = new File(graphPath);
        if (file.exists() && file.length() > 0) {//如果本地存在文件，获取本地文件bitmap
            bitmapNet = BitmapFactory.decodeFile(graphPath);
        } else {//否则，将网络图片转换成bitmap
            bitmapNet = BitmapUtils.returnBitMap(graphPath);
        }
        return bitmapNet;
    }

    /**
     * 将网络图片转换成bitmap
     */
    public static Bitmap returnBitMap(String urlImg) {
        try {
            //new一个URL对象
            URL url = new URL(urlImg);
            //打开链接
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            //设置请求方式为"GET"
            conn.setRequestMethod("GET");
            //超时响应时间为5秒
            conn.setConnectTimeout(5 * 1000);
            //如果访问成功，将图片写入流转换成字节类型，获取bitmap对象
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                byte[] data = readStream(conn.getInputStream());
                if (data != null) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeByteArray(data, 0, data.length, options);
                    int tempW = options.outWidth;
                    int tempH = options.outHeight;
                    options.inJustDecodeBounds = false;
                    if (tempW > 4000 || tempH > 4000) {
                        options.inSampleSize = 2;
                    }
                    options.inPreferredConfig = Bitmap.Config.ARGB_4444;
                    return BitmapFactory.decodeByteArray(data, 0, data.length, options);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将图片转换成字节类型
     *
     * @param inStream 图片的写入流
     * @return 图片的字节
     * @throws Exception
     */
    public static byte[] readStream(InputStream inStream) throws Exception {
        //创建字节输出流
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            //写入数据
            outStream.write(buffer, 0, len);
        }
        outStream.close();
        inStream.close();
        return outStream.toByteArray();
    }
}
