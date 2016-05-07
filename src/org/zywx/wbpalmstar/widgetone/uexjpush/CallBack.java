package org.zywx.wbpalmstar.widgetone.uexjpush;

/**
 * Created by ylt on 15/4/1.
 */
public interface CallBack {
    void onReceiveRegistration(String jsonData);
    void onReceiveMessage(String jsonData);
    void onReceiveNotification(String jsonData);
    void onReceiveNotificationOpen(String jsonData);
    void onReceiveConnectionChange(String jsonData);
}
