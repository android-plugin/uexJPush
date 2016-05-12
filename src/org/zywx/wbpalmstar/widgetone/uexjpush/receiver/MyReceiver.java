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
import org.zywx.wbpalmstar.widgetone.uexjpush.CallBack;
import org.zywx.wbpalmstar.widgetone.uexjpush.db.DBConstant;
import org.zywx.wbpalmstar.widgetone.uexjpush.db.DBFunction;
import org.zywx.wbpalmstar.widgetone.uexjpush.db.DBHelper;
import org.zywx.wbpalmstar.widgetone.uexjpush.utils.MLog;

import java.util.List;

import cn.jpush.android.api.JPushInterface;

public class MyReceiver extends BroadcastReceiver {

	// EUExJPush的引用
	public static CallBack callBack;

	// 缓存收到的Intent
	public static Intent offlineIntent = null;

	// 能收到的Intent必须含有该category
	public static final String CATEGORY = "org.zywx.wbpalmstar.widgetone.uexjpush.transit";

	// 删除数据库中的所有Intent广播
	public static final String BROADCAST_DELETE_ALL_INTENTS_IN_DB = "org.zywx.wbpalmstar.widgetone.uexjpush.BROADCAST_DELETE_INTENTS_IN_DB";

	@Override
	public void onReceive(Context context, Intent intent) {

		MLog.getIns().i("start");

		String action = intent.getAction();
		MLog.getIns().i("action = " + action);

		if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
			if (!isAppForground(context)) {
				runApp(context);
			}
		}
		if (callBack == null) {
			offlineIntent = intent;
			return;
		}
		offlineIntent = null;
		handleIntent(context, intent);
	}

	/**
	 * 处理Intent
	 * 
	 * @param context
	 * @param intent
	 */
	public static void handleIntent(Context context, Intent intent) {

		if (callBack == null) {
			MLog.getIns().e("callBack == null");
			return;
		}

		MLog.getIns().i("EUExJPush的实例callback = " + callBack.toString());
		Bundle bundle = intent.getExtras();

		/*
		 * SDK 向 JPush Server 注册所得到的注册 ID
		 */
		if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {

			String regId = bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID);
			MLog.getIns().i("接收Registration Id : " + regId);

			JSONObject jsonObject = new JSONObject();
			try {
				jsonObject.put("title", regId);
				callBack.onReceiveRegistration(jsonObject.toString());
			} catch (JSONException e) {
				e.printStackTrace();
				MLog.getIns().e(e);
			}
		}

		/*
		 * 收到了自定义消息 Push
		 */
		else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {

			MLog.getIns().i("接收到推送下来的自定义消息");

			callbackMessage(bundle);
		}

		/*
		 * 收到了通知 Push
		 */
		else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {

			MLog.getIns().i("接收到推送下来的通知");

			callbackNotification(bundle);
		}

		/*
		 * 用户点击了通知
		 */
		else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {

			MLog.getIns().i("用户点击打开了通知");

			callbackNotificationOpen(bundle);
		}

		/*
		 * 用户接受Rich Push Javascript 回调函数的intent
		 */
		else if (JPushInterface.ACTION_RICHPUSH_CALLBACK.equals(intent.getAction())) {

			MLog.getIns().i("用户收到到RICH PUSH CALLBACK: " + bundle.getString(JPushInterface.EXTRA_EXTRA));

			// 在这里根据 JPushInterface.EXTRA_EXTRA 的内容处理代码，比如打开新的Activity，
			// 打开一个网页等..

		}

		/*
		 * 接收网络变化 连接/断开 since 1.6.3
		 */
		else if (JPushInterface.ACTION_CONNECTION_CHANGE.equals(intent.getAction())) {

			boolean connected = intent.getBooleanExtra(JPushInterface.EXTRA_CONNECTION_CHANGE, false);
			MLog.getIns().i("action = " + intent.getAction() + " connected state change to " + connected);

			JSONObject jsonObject = new JSONObject();
			try {
				jsonObject.put("connect", connected ? 0 : 1);
				callBack.onReceiveConnectionChange(jsonObject.toString());
			} catch (JSONException e) {
				e.printStackTrace();
				MLog.getIns().e(e);
			}
		}

		/*
		 * 删除DB中所有Intent的广播
		 */
		if (intent.getAction() == BROADCAST_DELETE_ALL_INTENTS_IN_DB) {

			MLog.getIns().i("删除DB中所有Intent的广播");

			DBHelper helper = new DBHelper(context, DBConstant.DB_NAME, null, 1);
			SQLiteDatabase db = helper.getWritableDatabase();
			DBFunction.deleteAllIntents(db);
		}

		/*
		 * 未处理到的Intent
		 */
		else {
			MLog.getIns().i("Unhandled intent - " + intent.getAction());
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
