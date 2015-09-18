package org.zywx.wbpalmstar.widgetone.uexJPush;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import cn.jpush.android.api.JPushInterface;

public class MyReceiver extends BroadcastReceiver {

    public static CallBack callBack;

    public static Intent offlineIntent=null;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())){
            if (!isAppForground(context)){
                RunApp(context);
            }
        }
        if (callBack==null){
            offlineIntent=intent;
            return;
        }
        offlineIntent=null;
        handleIntent(context,intent);
    }

    public static void handleIntent(Context context, Intent intent){
        if (callBack==null){
            return;
        }
        Bundle bundle = intent.getExtras();
        if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
            String regId = bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID);
            JSONObject jsonObject=new JSONObject();
            try {
                jsonObject.put("title",regId);
                callBack.onReceiveRegistration(jsonObject.toString());
            } catch (JSONException e) {
            }
        } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
            callbackMessage(bundle);
        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
            callbackNotification(bundle);
        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
              callbackNotificationOpen(bundle);
        } else if (JPushInterface.ACTION_RICHPUSH_CALLBACK.equals(intent.getAction())) {

        } else if(JPushInterface.ACTION_CONNECTION_CHANGE.equals(intent.getAction())) {
            boolean connected = intent.getBooleanExtra(JPushInterface.EXTRA_CONNECTION_CHANGE, false);
            JSONObject jsonObject=new JSONObject();
            try {
                jsonObject.put("connect",connected?0:1);
                callBack.onReceiveConnectionChange(jsonObject.toString());
            } catch (JSONException e) {
            }
        }
    }

    public boolean isAppForground(Context mContext) {
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(mContext.getPackageName())) {
                return false;
            }
        }
        return true;
    }

    public static void RunApp(Context context) {
        String packageName=context.getApplicationInfo().packageName;
        PackageInfo pi;
        try {
            pi = context.getPackageManager().getPackageInfo(packageName, 0);
            Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
            resolveIntent.setPackage(pi.packageName);
            PackageManager pManager = context.getPackageManager();
            List<ResolveInfo> apps = pManager.queryIntentActivities(
                    resolveIntent, 0);
            ResolveInfo ri = apps.iterator().next();
            if (ri != null) {
                packageName = ri.activityInfo.packageName;
                String className = ri.activityInfo.name;
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ComponentName cn = new ComponentName(packageName, className);
                intent.setComponent(cn);
                context.startActivity(intent);
            }
        } catch (PackageManager.NameNotFoundException e) {
        }

    }


    private static void callbackNotificationOpen(Bundle bundle) {
        String title = bundle.getString(JPushInterface.EXTRA_NOTIFICATION_TITLE);
        String content = bundle.getString(JPushInterface.EXTRA_ALERT);
        String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
        int notificationId = bundle.getInt(JPushInterface.EXTRA_NOTIFICATION_ID);
        String msgId = bundle.getString(JPushInterface.EXTRA_MSG_ID);
        JSONObject jsonObject=new JSONObject();
        put(jsonObject,"title",title);
        put(jsonObject,"content",content);
        if (extras!=null) {
            try {
                JSONObject extrasObject = new JSONObject(extras);
                put(jsonObject, "extras", extrasObject);
            } catch (JSONException e) {
                put(jsonObject, "extras", extras);
                Log.e("JPush_Receiver", "json parse extras param exception");
            }
        }
        put(jsonObject,"notificationId",notificationId);
        put(jsonObject,"msgId",msgId);
        callBack.onReceiveNotificationOpen(jsonObject.toString());
    }

    private static void callbackNotification(Bundle bundle) {
        String title = bundle.getString(JPushInterface.EXTRA_NOTIFICATION_TITLE);
        String content = bundle.getString(JPushInterface.EXTRA_ALERT);
        String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
        int notificationId = bundle.getInt(JPushInterface.EXTRA_NOTIFICATION_ID);
        String type = bundle.getString(JPushInterface.EXTRA_CONTENT_TYPE);
        String fileHtml = bundle.getString(JPushInterface.EXTRA_RICHPUSH_HTML_PATH);
        String fileStr = bundle.getString(JPushInterface.EXTRA_RICHPUSH_HTML_RES);
        String msgId = bundle.getString(JPushInterface.EXTRA_MSG_ID);
        JSONObject jsonObject=new JSONObject();
        put(jsonObject,"title",title);
        put(jsonObject,"content",content);
        if (extras!=null) {
            try {
                JSONObject extrasObject = new JSONObject(extras);
                put(jsonObject, "extras", extrasObject);
            } catch (JSONException e) {
                put(jsonObject, "extras", extras);
                Log.e("JPush_Receiver", "json parse extras param exception");
            }
        }
        put(jsonObject,"notificationId",notificationId);
        put(jsonObject,"type",type);
        put(jsonObject,"fileHtml",fileHtml);
        put(jsonObject,"fileStr",fileStr);
        put(jsonObject,"msgId",msgId);
        callBack.onReceiveNotification(jsonObject.toString());
    }

    //send msg to MainActivity
    private static void callbackMessage(Bundle bundle) {
        JSONObject jsonObject=new JSONObject();
        String title = bundle.getString(JPushInterface.EXTRA_TITLE);
        String message = bundle.getString(JPushInterface.EXTRA_MESSAGE);
        String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
        String type = bundle.getString(JPushInterface.EXTRA_CONTENT_TYPE);
        String file = bundle.getString(JPushInterface.EXTRA_RICHPUSH_FILE_PATH);
        String msgId = bundle.getString(JPushInterface.EXTRA_MSG_ID);
        put(jsonObject, "title", title);
        put(jsonObject, "message",message);
        if (extras!=null) {
            try {
                JSONObject extrasObject = new JSONObject(extras);
                put(jsonObject, "extras", extrasObject);
            } catch (JSONException e) {
                put(jsonObject, "extras", extras);
                Log.e("JPush_Receiver", "json parse extras param exception");
            }
        }
        put(jsonObject,"type",type);
        put(jsonObject,"file",file);
        put(jsonObject,"msgId",msgId);
        callBack.onReceiveMessage(jsonObject.toString());
    }

    private static void put(JSONObject jsonObject,String key,Object value){
        if (value!=null){
            try {
                jsonObject.put(key,value);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static void setCallBack(CallBack temp) {
        callBack = temp;
    }
}
