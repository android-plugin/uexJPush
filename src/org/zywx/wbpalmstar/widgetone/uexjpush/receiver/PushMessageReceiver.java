package org.zywx.wbpalmstar.widgetone.uexjpush.receiver;

import android.content.Context;

import org.zywx.wbpalmstar.base.BDebug;

import cn.jpush.android.api.NotificationMessage;
import cn.jpush.android.service.JPushMessageReceiver;

/**
 * JPush3.3.0 之后的新版本（继承 JPush 提供的 JPushMessageReceiver 类）
 */
public class PushMessageReceiver extends JPushMessageReceiver {

    private static final String TAG = "PushMessageReceiver";

    @Override
    public void onNotifyMessageArrived(Context context, NotificationMessage notificationMessage) {
        super.onNotifyMessageArrived(context, notificationMessage);
        BDebug.i(TAG, "onNotifyMessageArrived");
    }

    @Override
    public void onNotifyMessageOpened(Context context, NotificationMessage notificationMessage) {
        super.onNotifyMessageOpened(context, notificationMessage);
        BDebug.i(TAG, "onNotifyMessageOpened");
    }
}
