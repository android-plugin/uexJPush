package org.zywx.wbpalmstar.widgetone.uexjpush.receiver;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.widgetone.uexjpush.CallBack;
import org.zywx.wbpalmstar.widgetone.uexjpush.db.DBConstant;
import org.zywx.wbpalmstar.widgetone.uexjpush.db.DBFunction;
import org.zywx.wbpalmstar.widgetone.uexjpush.db.DBHelper;
import org.zywx.wbpalmstar.widgetone.uexjpush.utils.SharedPreferencesUtil;

import java.util.List;

import cn.jpush.android.api.JPushInterface;

public class MyReceiver extends BroadcastReceiver {

    private static final String TAG = "uexJPush-MyReceiver";

    private DBHelper mDBHelper;
    private SQLiteDatabase mDB;


    // EUExJPush的引用
    public static CallBack callBack;

    // 缓存收到的Intent
    public static Intent offlineIntent = null;

    // 能收到的Intent必须含有该category
    public static final String CATEGORY = "org.zywx.wbpalmstar.widgetone.uexjpush.transit";

    // 删除数据库中的所有Intent广播
    public static final String BROADCAST_DELETE_ALL_INTENTS_IN_DB = "org.zywx.wbpalmstar.widgetone.uexjpush.BROADCAST_DELETE_INTENTS_IN_DB";
    // 应用进入前台的事件通知
    public static final String BROADCAST_ON_APP_ENTER_FORGROUND = "org.zywx.wbpalmstar.widgetone.uexjpush.BROADCAST_ON_APP_ENTER_FORGROUND";
    // 角标插件交互action
    public final static String ACTION_BADGE_CHANGE = "org.zywx.badge.changed";
    // 角标数目Intent key
    public final static String INTENT_KEY_BADGE_NUM = "badgeNum";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
            BDebug.i(TAG, "[AppCan]MyReceiver接受到点击通知栏的消息："+intent.getAction());
            if (!isAppForground(context)) {// 如果App不在前台
                BDebug.i(TAG, "[AppCan]MyReceiver判断APP是否在前台："+intent.getAction());
                runApp(context);// 启动App
            }
        }
        BDebug.e(TAG, "[AppCan]MyReceiver广播接受到消息onReceive："+intent.getAction());
        String action = intent.getAction();
        BDebug.d(TAG, "action = " + action);
        if (callBack == null) {
            BDebug.d(TAG, "插件还未初始化，先缓存 intent");

            // 初始化数据库
            initDB(context.getApplicationContext());
            // 把Intent存到数据库中
            DBFunction.insertIntent(mDB, intent);
            return;
        }
        handleIntent(context, intent);


    }


    /**
     * 初始化数据库
     *
     * @param context
     */
    private void initDB(Context context) {

        BDebug.d("start");

        if (mDBHelper == null) {
            mDBHelper = new DBHelper(context, DBConstant.DB_NAME, null, 1);
        }
        mDB = mDBHelper.getWritableDatabase();
    }

    /**
     * 处理Intent
     *
     * @param context
     * @param intent
     */
    public void handleIntent(Context context, Intent intent) {
        BDebug.i(TAG, "[AppCan]MyReceiver处理接受到Action====handleIntent："+intent.getAction());
        if (callBack == null) {
            BDebug.e(TAG, "handleIntent callBack == null");
            return;
        }

        Bundle bundle = intent.getExtras();

		/*
         * SDK 向 JPush Server 注册所得到的注册 ID
		 */
        if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
            BDebug.i(TAG, "[AppCan]MyReceiver处理接受到Actiont："+intent.getAction());
            String regId = bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID);
            BDebug.i(TAG, "接收Registration Id : " + regId);

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("title", regId);
                callBack.onReceiveRegistration(jsonObject.toString());
            } catch (JSONException e) {
                e.printStackTrace();
                BDebug.e(e);
            }
        }

		/*
		 * 收到了自定义消息 Push
		 */
        else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
            BDebug.i(TAG, "[AppCan]MyReceiver收到了自定义消息 PushActiont："+intent.getAction());
            BDebug.i(TAG, "接收到推送下来的自定义消息");

            callbackMessage(bundle);
        }

		/*
		 * 收到了通知 Push
		 */
        else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
            BDebug.i(TAG, "[AppCan]MyReceiver收到了通知 Push PushActiont："+intent.getAction());
            // 增长角标
            increaseBadgeNum(context);
            callbackNotification(bundle);
        }

		/*
		 * 用户点击了通知
		 */
        else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
            BDebug.i(TAG, "[AppCan]MyReceiver用户点击了通知 Push Action ："+intent.getAction());
            // 清空角标
            clearBadgeNum(context);
            callbackNotificationOpen(bundle);
        }

		/*
		 * 用户接受Rich Push Javascript 回调函数的intent
		 */
        else if (JPushInterface.ACTION_RICHPUSH_CALLBACK.equals(intent.getAction())) {
            BDebug.i(TAG, "[AppCan]MyReceiver用户接受Rich Push Javascript 回调函数的intent Push Action ："+intent.getAction());
            BDebug.i(TAG, "用户收到到RICH PUSH CALLBACK: " + bundle.getString(JPushInterface.EXTRA_EXTRA));

            // 在这里根据 JPushInterface.EXTRA_EXTRA 的内容处理代码，比如打开新的Activity，
            // 打开一个网页等..

        }

		/*
		 * 接收网络变化 连接/断开 since 1.6.3
		 */
        else if (JPushInterface.ACTION_CONNECTION_CHANGE.equals(intent.getAction())) {
            BDebug.i(TAG, "[AppCan]MyReceiver接收网络变化 连接/断开 Push Action ："+intent.getAction());

            boolean connected = intent.getBooleanExtra(JPushInterface.EXTRA_CONNECTION_CHANGE, false);
            BDebug.i(TAG, "action = " + intent.getAction() + " connected state change to " + connected);

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("connect", connected ? 0 : 1);
                callBack.onReceiveConnectionChange(jsonObject.toString());
            } catch (JSONException e) {
                e.printStackTrace();
                BDebug.e(e);
            }
        }

		/*
		 * 删除DB中所有Intent的广播
		 */
        else if ( BROADCAST_DELETE_ALL_INTENTS_IN_DB.equals(intent.getAction())) {
            BDebug.i(TAG, "[AppCan]MyReceiver 删除DB中所有Intent的广播：");
            DBHelper helper = new DBHelper(context, DBConstant.DB_NAME, null, 1);
            SQLiteDatabase db = helper.getWritableDatabase();
            DBFunction.deleteAllIntents(db);
        }
        /*
         * 应用进入前台通知
         * （可能需要清空角标，或者其他操作等）
         */
        else if(BROADCAST_ON_APP_ENTER_FORGROUND.equals(intent.getAction())){
            BDebug.i(TAG, "[AppCan]MyReceiver 应用进入前台通知");
            // 清空角标
            clearBadgeNum(context);
        }

		/*
		 * 未处理到的Intent
		 */
        else {
            BDebug.e(TAG, "[AppCan]MyReceiver 未处理到的Intent：");
            BDebug.d(TAG, "Unhandled intent - " + intent.getAction());
        }
    }

    /**
     * 判断App是否在前台
     *
     * @param mContext
     * @return
     */
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

    /**
     * 发送角标变更的通知。详情参见uexShortcutBadger插件的接收逻辑。
     *
     * @param context
     * @param badgeNum 角标
     */
    private void sendBadgeChangedBroadcast(Context context, int badgeNum){
        Intent intent = new Intent();
        intent.setAction(ACTION_BADGE_CHANGE);
        intent.setPackage(context.getPackageName());
        intent.putExtra(INTENT_KEY_BADGE_NUM, badgeNum);
        context.sendBroadcast(intent);
    }

    /**
     * 增长角标并设置
     *
     * @param context
     */
    private void increaseBadgeNum(Context context){
        boolean isBadgeEnable = SharedPreferencesUtil.getBadgerEnable(context);
        BDebug.i("uexJPush increaseBadgeNum isBadgeEnable: " + isBadgeEnable);
        if (isBadgeEnable){
            int currentBadgeNum = SharedPreferencesUtil.getBadgerNum(context);
            currentBadgeNum++;
            SharedPreferencesUtil.saveBadgerNum(context, currentBadgeNum);
            BDebug.i("uexJPush increaseBadgeNum: " + currentBadgeNum);
            // 引擎3.3.6版本的极光SDK，角标API并没有生效，所以弃用之，直接使用uexShortcutBadger插件的方式实现角标控制
//            JPushInterface.setBadgeNumber(context, currentBadgeNum);
            sendBadgeChangedBroadcast(context, currentBadgeNum);
        }
    }

    /**
     * 清空角标
     *
     * @param context
     */
    private void clearBadgeNum(Context context){
        boolean isBadgeEnable = SharedPreferencesUtil.getBadgerEnable(context);
        BDebug.i("uexJPush clearBadgeNum isBadgeEnable: " + isBadgeEnable);
        if (isBadgeEnable){
            SharedPreferencesUtil.clearBadgerNum(context);
            BDebug.i("uexJPush clearBadgeNum");
            // 引擎3.3.6版本的极光SDK，角标API并没有生效，所以弃用之，直接使用uexShortcutBadger插件的方式实现角标控制
//            JPushInterface.setBadgeNumber(context, 0);
            sendBadgeChangedBroadcast(context, 0);
        }
    }

    /**
     * 启动App
     *
     * @param context
     */
    public static void runApp(Context context) {
        String packageName = context.getApplicationInfo().packageName;
        PackageInfo pi;
        try {
            pi = context.getPackageManager().getPackageInfo(packageName, 0);
            Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
            resolveIntent.setPackage(pi.packageName);
            PackageManager pManager = context.getPackageManager();
            List<ResolveInfo> apps = pManager.queryIntentActivities(resolveIntent, 0);
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
        JSONObject jsonObject = new JSONObject();
        put(jsonObject, "title", title);
        put(jsonObject, "content", content);
        if (extras != null) {
            try {
                JSONObject extrasObject = new JSONObject(extras);
                put(jsonObject, "extras", extrasObject);
            } catch (JSONException e) {
                put(jsonObject, "extras", extras);
                Log.e("JPush_Receiver", "json parse extras param exception");
            }
        }
        put(jsonObject, "notificationId", notificationId);
        put(jsonObject, "msgId", msgId);
        BDebug.e("AppCan:", "[AppCan]MyReceiver点击用户自定义处理广播");
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
        JSONObject jsonObject = new JSONObject();
        put(jsonObject, "title", title);
        put(jsonObject, "content", content);
        if (extras != null) {
            try {
                JSONObject extrasObject = new JSONObject(extras);
                put(jsonObject, "extras", extrasObject);
            } catch (JSONException e) {
                put(jsonObject, "extras", extras);
                Log.e("JPush_Receiver", "json parse extras param exception");
            }
        }
        put(jsonObject, "notificationId", notificationId);
        put(jsonObject, "type", type);
        put(jsonObject, "fileHtml", fileHtml);
        put(jsonObject, "fileStr", fileStr);
        put(jsonObject, "msgId", msgId);
        callBack.onReceiveNotification(jsonObject.toString());
    }

    // send msg to MainActivity
    private static void callbackMessage(Bundle bundle) {
        JSONObject jsonObject = new JSONObject();
        String title = bundle.getString(JPushInterface.EXTRA_TITLE);
        String message = bundle.getString(JPushInterface.EXTRA_MESSAGE);
        String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
        String type = bundle.getString(JPushInterface.EXTRA_CONTENT_TYPE);
        String file = bundle.getString(JPushInterface.EXTRA_RICHPUSH_FILE_PATH);
        String msgId = bundle.getString(JPushInterface.EXTRA_MSG_ID);
        put(jsonObject, "title", title);
        put(jsonObject, "message", message);
        if (extras != null) {
            try {
                JSONObject extrasObject = new JSONObject(extras);
                put(jsonObject, "extras", extrasObject);
            } catch (JSONException e) {
                put(jsonObject, "extras", extras);
                Log.e("JPush_Receiver", "json parse extras param exception");
            }
        }
        put(jsonObject, "type", type);
        put(jsonObject, "file", file);
        put(jsonObject, "msgId", msgId);
        callBack.onReceiveMessage(jsonObject.toString());
    }

    private static void put(JSONObject jsonObject, String key, Object value) {
        if (value != null) {
            try {
                jsonObject.put(key, value);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static void setCallBack(CallBack temp) {
        callBack = temp;
    }
}
