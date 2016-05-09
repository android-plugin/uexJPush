package org.zywx.wbpalmstar.widgetone.uexjpush.utils;

import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;
import org.zywx.wbpalmstar.widgetone.uexjpush.receiver.MyReceiver;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import cn.jpush.android.api.JPushInterface;

/**
 * SharedPreferences工具类
 * 
 * @author waka
 * @version createTime:2016年5月7日 上午10:35:31
 */
public class SharedPreferencesUtil {

	public static final String SP_INTENT = "intent";

	public static final String SP_INTENT_ACTION = "action";

	/**
	 * 将Intent存入SharedPreferences中
	 * 
	 * @param context
	 * @param intent
	 */
	public static boolean saveIntent(Context context, Intent intent) {

		String action = intent.getAction();
		Bundle bundle = intent.getExtras();
		MLog.getIns().i("action = " + action);
		MLog.getIns().i("bundle = " + printBundle(bundle));

		SharedPreferences.Editor editor = context.getSharedPreferences(SP_INTENT, Context.MODE_MULTI_PROCESS).edit();// 获得SharedPreferences.Editor对象
		editor.putString(SP_INTENT_ACTION, action);// 不管其他的，先把action放进去

		// SDK 向 JPush Server 注册所得到的注册 ID
		if (action.equals(JPushInterface.ACTION_REGISTRATION_ID)) {

			editor.putString(JPushInterface.EXTRA_REGISTRATION_ID, bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID));
		}

		// 收到了自定义消息 Push
		else if (action.equals(JPushInterface.ACTION_MESSAGE_RECEIVED)) {
			editor.putString(JPushInterface.EXTRA_TITLE, bundle.getString(JPushInterface.EXTRA_TITLE));
			editor.putString(JPushInterface.EXTRA_MESSAGE, bundle.getString(JPushInterface.EXTRA_MESSAGE));
			editor.putString(JPushInterface.EXTRA_CONTENT_TYPE, bundle.getString(JPushInterface.EXTRA_CONTENT_TYPE));
			editor.putString(JPushInterface.EXTRA_RICHPUSH_FILE_PATH, bundle.getString(JPushInterface.EXTRA_RICHPUSH_FILE_PATH));
			editor.putString(JPushInterface.EXTRA_MSG_ID, bundle.getString(JPushInterface.EXTRA_MSG_ID));
			String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
			if (extras != null) {
				editor.putString(JPushInterface.EXTRA_EXTRA, extras);
			}
		}

		// 收到了通知 Push
		else if (action.equals(JPushInterface.ACTION_NOTIFICATION_RECEIVED)) {
			editor.putInt(JPushInterface.EXTRA_NOTIFICATION_ID, bundle.getInt(JPushInterface.EXTRA_NOTIFICATION_ID));
			editor.putString(JPushInterface.EXTRA_NOTIFICATION_TITLE, bundle.getString(JPushInterface.EXTRA_NOTIFICATION_TITLE));
			editor.putString(JPushInterface.EXTRA_ALERT, bundle.getString(JPushInterface.EXTRA_ALERT));
			editor.putString(JPushInterface.EXTRA_CONTENT_TYPE, bundle.getString(JPushInterface.EXTRA_CONTENT_TYPE));
			editor.putString(JPushInterface.EXTRA_RICHPUSH_HTML_PATH, bundle.getString(JPushInterface.EXTRA_RICHPUSH_HTML_PATH));
			editor.putString(JPushInterface.EXTRA_RICHPUSH_HTML_RES, bundle.getString(JPushInterface.EXTRA_RICHPUSH_HTML_RES));
			editor.putString(JPushInterface.EXTRA_MSG_ID, bundle.getString(JPushInterface.EXTRA_MSG_ID));
			String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
			if (extras != null) {
				editor.putString(JPushInterface.EXTRA_EXTRA, extras);
			}
		}

		// 用户点击了通知
		else if (action.equals(JPushInterface.ACTION_NOTIFICATION_OPENED)) {
			editor.putInt(JPushInterface.EXTRA_NOTIFICATION_ID, bundle.getInt(JPushInterface.EXTRA_NOTIFICATION_ID));
			editor.putString(JPushInterface.EXTRA_NOTIFICATION_TITLE, bundle.getString(JPushInterface.EXTRA_NOTIFICATION_TITLE));
			editor.putString(JPushInterface.EXTRA_ALERT, bundle.getString(JPushInterface.EXTRA_ALERT));
			editor.putString(JPushInterface.EXTRA_MSG_ID, bundle.getString(JPushInterface.EXTRA_MSG_ID));
			String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
			if (extras != null) {
				editor.putString(JPushInterface.EXTRA_EXTRA, extras);
			}
		}

		// 用户接受Rich Push Javascript 回调函数的intent
		else if (action.equals(JPushInterface.ACTION_RICHPUSH_CALLBACK)) {
			String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
			if (extras != null) {
				editor.putString(JPushInterface.EXTRA_EXTRA, extras);
			}
		}

		// 接收网络变化 连接/断开 since 1.6.3
		else if (action.equals(JPushInterface.ACTION_CONNECTION_CHANGE)) {
			editor.putBoolean(JPushInterface.EXTRA_CONNECTION_CHANGE, intent.getBooleanExtra(JPushInterface.EXTRA_CONNECTION_CHANGE, false));
		}

		return editor.commit();
	}

	/**
	 * 得到Intent
	 * 
	 * @param context
	 * @return
	 */
	public static Intent getIntent(Context context) {

		SharedPreferences sp = context.getSharedPreferences(SP_INTENT, Context.MODE_MULTI_PROCESS);// 获得SharedPreferences.Editor对象

		String action = sp.getString(SP_INTENT_ACTION, "");
		if (action.isEmpty()) {
			MLog.getIns().e("action.isEmpty()");
			return null;
		}

		MLog.getIns().i("action = " + action);

		Bundle bundle = new Bundle();
		Intent intent = new Intent();
		intent.setAction(action);
		intent.addCategory(MyReceiver.CATEGORY);

		// SDK 向 JPush Server 注册所得到的注册 ID
		if (action.equals(JPushInterface.ACTION_REGISTRATION_ID)) {
			bundle.putString(JPushInterface.EXTRA_REGISTRATION_ID, sp.getString(JPushInterface.EXTRA_REGISTRATION_ID, ""));
		}

		// 收到了自定义消息 Push
		else if (action.equals(JPushInterface.ACTION_MESSAGE_RECEIVED)) {
			bundle.putString(JPushInterface.EXTRA_TITLE, sp.getString(JPushInterface.EXTRA_TITLE, ""));
			bundle.putString(JPushInterface.EXTRA_MESSAGE, sp.getString(JPushInterface.EXTRA_MESSAGE, ""));
			bundle.putString(JPushInterface.EXTRA_CONTENT_TYPE, sp.getString(JPushInterface.EXTRA_CONTENT_TYPE, ""));
			bundle.putString(JPushInterface.EXTRA_RICHPUSH_FILE_PATH, sp.getString(JPushInterface.EXTRA_RICHPUSH_FILE_PATH, ""));
			bundle.putString(JPushInterface.EXTRA_MSG_ID, sp.getString(JPushInterface.EXTRA_MSG_ID, ""));
			String extras = sp.getString(JPushInterface.EXTRA_EXTRA, "");
			if (!extras.isEmpty()) {
				bundle.putString(JPushInterface.EXTRA_EXTRA, extras);
			}
		}

		// 收到了通知 Push
		else if (action.equals(JPushInterface.ACTION_NOTIFICATION_RECEIVED)) {
			bundle.putInt(JPushInterface.EXTRA_NOTIFICATION_ID, sp.getInt(JPushInterface.EXTRA_NOTIFICATION_ID, -1));
			bundle.putString(JPushInterface.EXTRA_NOTIFICATION_TITLE, sp.getString(JPushInterface.EXTRA_NOTIFICATION_TITLE, ""));
			bundle.putString(JPushInterface.EXTRA_ALERT, sp.getString(JPushInterface.EXTRA_ALERT, ""));
			bundle.putString(JPushInterface.EXTRA_CONTENT_TYPE, sp.getString(JPushInterface.EXTRA_CONTENT_TYPE, ""));
			bundle.putString(JPushInterface.EXTRA_RICHPUSH_HTML_PATH, sp.getString(JPushInterface.EXTRA_RICHPUSH_HTML_PATH, ""));
			bundle.putString(JPushInterface.EXTRA_RICHPUSH_HTML_RES, sp.getString(JPushInterface.EXTRA_RICHPUSH_HTML_RES, ""));
			bundle.putString(JPushInterface.EXTRA_MSG_ID, sp.getString(JPushInterface.EXTRA_MSG_ID, ""));
			String extras = sp.getString(JPushInterface.EXTRA_EXTRA, "");
			if (!extras.isEmpty()) {
				bundle.putString(JPushInterface.EXTRA_EXTRA, extras);
			}
		}

		// 用户点击了通知
		else if (action.equals(JPushInterface.ACTION_NOTIFICATION_OPENED)) {
			bundle.putInt(JPushInterface.EXTRA_NOTIFICATION_ID, sp.getInt(JPushInterface.EXTRA_NOTIFICATION_ID, -1));
			bundle.putString(JPushInterface.EXTRA_NOTIFICATION_TITLE, sp.getString(JPushInterface.EXTRA_NOTIFICATION_TITLE, ""));
			bundle.putString(JPushInterface.EXTRA_ALERT, sp.getString(JPushInterface.EXTRA_ALERT, ""));
			bundle.putString(JPushInterface.EXTRA_MSG_ID, sp.getString(JPushInterface.EXTRA_MSG_ID, ""));
			String extras = sp.getString(JPushInterface.EXTRA_EXTRA, "");
			if (!extras.isEmpty()) {
				bundle.putString(JPushInterface.EXTRA_EXTRA, extras);
			}
		}

		// 用户接受Rich Push Javascript 回调函数的intent
		else if (action.equals(JPushInterface.ACTION_RICHPUSH_CALLBACK)) {
			String extras = sp.getString(JPushInterface.EXTRA_EXTRA, "");
			if (!extras.isEmpty()) {
				bundle.putString(JPushInterface.EXTRA_EXTRA, extras);
			}
		}

		// 接收网络变化 连接/断开 since 1.6.3
		else if (action.equals(JPushInterface.ACTION_CONNECTION_CHANGE)) {
			intent.putExtra(JPushInterface.EXTRA_CONNECTION_CHANGE, sp.getBoolean(JPushInterface.EXTRA_CONNECTION_CHANGE, false));
		}

		intent.putExtras(bundle);

		return intent;

	}

	/**
	 * 清除SP中的数据
	 * 
	 * @param context
	 * @return
	 */
	public static boolean clear(Context context) {

		MLog.getIns().i("");

		SharedPreferences.Editor editor = context.getSharedPreferences(SP_INTENT, Context.MODE_MULTI_PROCESS).edit();// 获得SharedPreferences.Editor对象
		editor.clear();
		boolean result = editor.commit();
		MLog.getIns().d("清除SP中的Intent结果 = " + result);

		return result;
	}

	/**
	 * 打印所有的 intent extra 数据
	 * 
	 * @param bundle
	 * @return
	 */
	private static String printBundle(Bundle bundle) {

		StringBuilder sb = new StringBuilder();

		for (String key : bundle.keySet()) {

			if (key.equals(JPushInterface.EXTRA_NOTIFICATION_ID)) {
				sb.append("\nkey:" + key + ", value:" + bundle.getInt(key));
			} else if (key.equals(JPushInterface.EXTRA_CONNECTION_CHANGE)) {
				sb.append("\nkey:" + key + ", value:" + bundle.getBoolean(key));
			} else if (key.equals(JPushInterface.EXTRA_EXTRA)) {
				if (bundle.getString(JPushInterface.EXTRA_EXTRA).isEmpty()) {
					MLog.getIns().i("This message has no Extra data");
					continue;
				}

				try {

					JSONObject json = new JSONObject(bundle.getString(JPushInterface.EXTRA_EXTRA));
					@SuppressWarnings("unchecked")
					Iterator<String> it = json.keys();

					while (it.hasNext()) {
						String myKey = it.next().toString();
						sb.append("\nkey:" + key + ", value: [" + myKey + " - " + json.optString(myKey) + "]");
					}

				} catch (JSONException e) {
					MLog.getIns().e("Get message extra JSON error!");
					MLog.getIns().e(e);
				}

			} else {
				sb.append("\nkey:" + key + ", value:" + bundle.getString(key));
			}
		}
		return sb.toString();
	}
}
