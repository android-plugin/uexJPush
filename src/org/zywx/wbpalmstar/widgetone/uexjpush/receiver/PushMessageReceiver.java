package org.zywx.wbpalmstar.widgetone.uexjpush.receiver;

import android.content.Context;

import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.engine.DataHelper;
import org.zywx.wbpalmstar.widgetone.uexjpush.CallBack;
import org.zywx.wbpalmstar.widgetone.uexjpush.vo.SetTagsResultVO;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import cn.jpush.android.api.JPushMessage;
import cn.jpush.android.api.NotificationMessage;
import cn.jpush.android.service.JPushMessageReceiver;

/**
 * JPush3.3.0 之后的新版本（继承 JPush 提供的 JPushMessageReceiver 类）
 * 目前过渡阶段，依然使用了旧的MyReceiver（因为逻辑改起来比较多，没有进行），只要在本类覆写的方法中，同时调用父类的方法，即可使SDK内部新旧逻辑同时执行。
 */
public class PushMessageReceiver extends JPushMessageReceiver {

    private static final String TAG = "PushMessageReceiver";

    private static CallBack mEUExCallbackInstance;

    /**
     * 设置入口类对象，用于向前端传递回调
     *
     * @param callback
     */
    public static void setCallback(CallBack callback) {
        mEUExCallbackInstance = callback;
    }

    @Override
    public void onNotifyMessageArrived(Context context, NotificationMessage notificationMessage) {
        super.onNotifyMessageArrived(context, notificationMessage);
        // 已经在MyReceiver中处理
        BDebug.i(TAG, "onNotifyMessageArrived");
    }

    @Override
    public void onNotifyMessageOpened(Context context, NotificationMessage notificationMessage) {
        super.onNotifyMessageOpened(context, notificationMessage);
        // 已经在MyReceiver中处理
        BDebug.i(TAG, "onNotifyMessageOpened");
    }

    @Override
    public void onConnected(Context context, boolean b) {
        super.onConnected(context, b);
    }

    @Override
    public void onTagOperatorResult(Context context, JPushMessage jPushMessage) {
        super.onTagOperatorResult(context, jPushMessage);
        int sequence = jPushMessage.getSequence(); // 每次回调的序号，对应调用时传入的值。目前用于与回调ID对应。
        int errorCode = jPushMessage.getErrorCode();
        String alias = jPushMessage.getAlias();
        Set<String> tagSet = jPushMessage.getTags();
        mEUExCallbackInstance.onReceiveTagResult(errorCode, sequence, alias, tagSet);
    }

    @Override
    public void onAliasOperatorResult(Context context, JPushMessage jPushMessage) {
        super.onAliasOperatorResult(context, jPushMessage);
        int sequence = jPushMessage.getSequence(); // 每次回调的序号，对应调用时传入的值。目前用于与回调ID对应。
        int errorCode = jPushMessage.getErrorCode();
        String alias = jPushMessage.getAlias();
        Set<String> tagSet = jPushMessage.getTags();
        mEUExCallbackInstance.onReceiveAliasResult(errorCode, sequence, alias, tagSet);
    }
}
