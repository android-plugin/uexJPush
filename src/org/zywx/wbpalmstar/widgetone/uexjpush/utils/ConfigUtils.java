package org.zywx.wbpalmstar.widgetone.uexjpush.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.zywx.wbpalmstar.acedes.ACEDes;
import org.zywx.wbpalmstar.base.BUtility;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


/**
 * File Description: 读取config配置
 * <p>
 * Created by sandy with Email: sandy1108@163.com at Date: 2022/6/26.
 */
public class ConfigUtils {

    private static final String TAG = "ConfigUtils";

    private static final String CONFIG_KEY_JPUSH = "JPushConfig";
    private static final String CONFIG_KEY_PLATFORM = "platform";
    private static final String CONFIG_VALUE_ANDROID = "Android";

    /**
     * 获得config文件中指定标签的值,传入指定的标签名,返回该标签的值
     *
     * @param context
     * @return
     */
    public static String getConfigLabelValue(Context context) {
        // 如果传入的标签不为空（包含null和""两种情况）
        String configFile = context.getFilesDir() + "/widget/config.xml";// 获得沙箱路径,getFilesDir()用于获取/data/data//files目录
        InputStream inputStream = null;
        InputStream is1 = null;
        InputStream is2 = null;
        File file = new File(configFile);
        // 若果从沙箱中读取不到，则再从assets文件夹中读
        if (!file.exists()) {
            try {
                inputStream = context.getAssets().open("widget/config.xml");
            } catch (IOException e) {
                Log.i(TAG, "getAssets IOException");
                e.printStackTrace();
            }
        } else {
            try {
                inputStream = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                Log.i(TAG, "FileNotFoundException");
                e.printStackTrace();
            }
        }

        // 正戏开始了，如果输入流不为空
        if (inputStream != null) {
            try {
                XmlPullParser xmlPullParser = Xml.newPullParser();// 使用Xml的静态方法生成语法分析器
                // 先判断是否加密
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                byte[] buffer = new byte[1024];
                int len;
                while ((len = inputStream.read(buffer)) > -1) {
                    baos.write(buffer, 0, len);
                }
                baos.flush();

                is1 = new ByteArrayInputStream(baos.toByteArray());
                is2 = new ByteArrayInputStream(baos.toByteArray());

                boolean isV = ACEDes.isEncrypted(is1);

                if (isV) {
                    InputStream resStream = null;
                    byte[] data = null;
                    String fileName = "config";
                    String result = null;
                    data = BUtility.transStreamToBytes(is2, is2.available());
                    if (ACEDes.getContext() == null) {
                        ACEDes.setContext(context.getApplicationContext());
                    }
                    result = ACEDes.htmlDecode(data, fileName);
                    resStream = new ByteArrayInputStream(result.getBytes());
                    xmlPullParser.setInput(resStream, "utf-8");
                } else {
                    xmlPullParser.setInput(is2, "utf-8");
                }

                int eventType = xmlPullParser.getEventType();// 获得解析到的事件类别，这里有开始文档，结束文档，开始标签，结束标签，文本等等事件
                // 循环直到文档结束
                boolean needContinue = true;
                while (needContinue) {
                    switch (eventType) {
                        // 事件若是开始标签
                        case XmlPullParser.START_TAG:
                            // 如果该标签是传入的标签，则value=该标签的值
                            if (xmlPullParser.getName().equals(CONFIG_KEY_JPUSH)) {
                                try {
                                    String paltform = xmlPullParser.getAttributeValue("", CONFIG_KEY_PLATFORM);
                                    if (TextUtils.isEmpty(paltform) || CONFIG_VALUE_ANDROID.equals(paltform)) {
                                        String key = xmlPullParser.nextText();
                                        return key;
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            break;
                        case XmlPullParser.END_DOCUMENT:
                            needContinue = false;
                            break;
                        default:
                            break;
                    }
                    try {
                        eventType = xmlPullParser.next();// 获取下一个事件
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // 如果inputStream不为空，释放掉
                if (inputStream != null) {
                    try {
                        inputStream.close();
                        inputStream = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } // end finally
        } // end if inputStream
        return null;
    }
}

