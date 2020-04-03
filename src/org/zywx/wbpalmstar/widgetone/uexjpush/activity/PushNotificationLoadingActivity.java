package org.zywx.wbpalmstar.widgetone.uexjpush.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.WindowManager;

import org.json.JSONObject;
import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.engine.LoadingActivity;
import org.zywx.wbpalmstar.widgetone.uexjpush.receiver.MyReceiver;

import cn.jpush.android.api.JPushInterface;

/**
 * File Description: 用于接收厂商推送消息通知的点击事件和消息内容
 * <p>
 *     参考了极光客服提供的在线文档
 *     https://www.yuque.com/docs/share/307d6d68-0cc2-41b6-935b-4ca8c77c63d5
 *
 * Created by zhangyipeng with Email: sandy1108@163.com at Date: 2020-04-03.
 */
public class PushNotificationLoadingActivity extends Activity {

    private static final String TAG = "PushNotificationLoading";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        handlePushMessage();
        goToAppCanEngine();
        finish();
    }

    /**
     * 处理厂商推送消息内容，若成功获取到，则将消息发送给极光插件广播接收器统一处理。
     *
     *  消息格式实例：
     *  {
     *     "n_extras": {},
     *     "n_title": "掌上深航消息",
     *     "n_content": "欢迎使用掌上深航！",
     *     "msg_id": 58546848643676418,
     *     "show_type": 4,
     *     "rom_type": 2
     * }
     */
    private void handlePushMessage() {
        try {
            Intent intent = getIntent();
            String pushMessage = null;
            if (intent != null) {
                // 华为推送处理逻辑
                pushMessage = getHuaWeiPushMessage();
                if (TextUtils.isEmpty(pushMessage)){
                    // 小米 魅族 vivo oppo推送处理逻辑
                    pushMessage = getOtherPushMessage();
                }
            }
            BDebug.i("handlePushMessage, pushMessage: " + pushMessage);
            // 解析厂商推送过来的消息内容，转换为极光推送的消息送达广播Intent形式，发送到相同的逻辑中处理
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(JPushInterface.ACTION_NOTIFICATION_OPENED);
//            broadcastIntent.setAction(MyReceiver.BROADCAST_ON_USER_CLICK_THIRD_PUSH_NOTIFICATION);
            broadcastIntent.setPackage(getPackageName());
            JSONObject pushMsgJson = new JSONObject(pushMessage);
            String title = pushMsgJson.getString("n_title");
            String content = pushMsgJson.getString("n_content");
            String msgId = pushMsgJson.getString("msg_id");
            String extras = pushMsgJson.getString("n_extras");
            Bundle extrasBundle = new Bundle();
            extrasBundle.putString(JPushInterface.EXTRA_NOTIFICATION_TITLE, title);
            extrasBundle.putString(JPushInterface.EXTRA_ALERT, content);
            extrasBundle.putString(JPushInterface.EXTRA_MSG_ID, msgId);
            extrasBundle.putString(JPushInterface.EXTRA_EXTRA, extras);
            broadcastIntent.putExtras(extrasBundle);
            sendBroadcast(broadcastIntent);
        } catch (Exception exception) {
            BDebug.w(TAG, "handlePushMessage", exception);
        }
    }

    /**
     * 获取华为厂商推送消息
     *
     */
    private String getHuaWeiPushMessage(){
        Uri uri = getIntent().getData();
        if (uri != null){
            return uri.toString();
        }else{
            return null;
        }
    }

    /**
     * 获取其他厂商推送消息（小米 魅族 vivo oppo等）
     *
     */
    private String getOtherPushMessage(){
        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            return bundle.getString("JMessageExtra");
        }else{
            return null;
        }
    }

    /**
     * 跳转到AppCan引擎中
     */
    private void goToAppCanEngine(){
        Intent intent = new Intent(this, LoadingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
