package org.zywx.wbpalmstar.widgetone.uexjpush.db;

import java.util.ArrayList;
import java.util.List;

import org.zywx.wbpalmstar.widgetone.uexjpush.receiver.MyReceiver;
import org.zywx.wbpalmstar.widgetone.uexjpush.utils.MLog;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import cn.jpush.android.api.JPushInterface;

/**
 * 数据库功能类
 * 
 * @author waka
 * @version createTime:2016年5月7日 下午4:46:24
 */
public class DBFunction {

	/**
	 * 插入Intent数据到数据库中
	 * 
	 * @param intent
	 */
	public static synchronized void insertIntent(SQLiteDatabase db, Intent intent) {

		ContentValues values = new ContentValues();

		String action = intent.getAction();
		Bundle bundle = intent.getExtras();
		MLog.getIns().i("action = " + action);

		values.put(DBConstant.ACTION, action);

		// SDK 向 JPush Server 注册所得到的注册 ID
		if (action.equals(JPushInterface.ACTION_REGISTRATION_ID)) {
			values.put(DBConstant.EXTRA_REGISTRATION_ID, bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID));
		}

		// 收到了自定义消息 Push
		else if (action.equals(JPushInterface.ACTION_MESSAGE_RECEIVED)) {
			values.put(DBConstant.EXTRA_TITLE, bundle.getString(JPushInterface.EXTRA_TITLE));
			values.put(DBConstant.EXTRA_MESSAGE, bundle.getString(JPushInterface.EXTRA_MESSAGE));
			values.put(DBConstant.EXTRA_CONTENT_TYPE, bundle.getString(JPushInterface.EXTRA_CONTENT_TYPE));
			values.put(DBConstant.EXTRA_RICHPUSH_FILE_PATH, bundle.getString(JPushInterface.EXTRA_RICHPUSH_FILE_PATH));
			values.put(DBConstant.EXTRA_MSG_ID, bundle.getString(JPushInterface.EXTRA_MSG_ID));
			String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
			if (extras != null) {
				values.put(DBConstant.EXTRA_EXTRA, extras);
			}
		}

		// 收到了通知 Push
		else if (action.equals(JPushInterface.ACTION_NOTIFICATION_RECEIVED)) {
			values.put(DBConstant.EXTRA_NOTIFICATION_ID, bundle.getInt(JPushInterface.EXTRA_NOTIFICATION_ID));
			values.put(DBConstant.EXTRA_NOTIFICATION_TITLE, bundle.getString(JPushInterface.EXTRA_NOTIFICATION_TITLE));
			values.put(DBConstant.EXTRA_ALERT, bundle.getString(JPushInterface.EXTRA_ALERT));
			values.put(DBConstant.EXTRA_CONTENT_TYPE, bundle.getString(JPushInterface.EXTRA_CONTENT_TYPE));
			values.put(DBConstant.EXTRA_RICHPUSH_HTML_PATH, bundle.getString(JPushInterface.EXTRA_RICHPUSH_HTML_PATH));
			values.put(DBConstant.EXTRA_RICHPUSH_HTML_RES, bundle.getString(JPushInterface.EXTRA_RICHPUSH_HTML_RES));
			values.put(DBConstant.EXTRA_MSG_ID, bundle.getString(JPushInterface.EXTRA_MSG_ID));
			String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
			if (extras != null) {
				values.put(DBConstant.EXTRA_EXTRA, extras);
			}
		}

		// 用户点击了通知
		else if (action.equals(JPushInterface.ACTION_NOTIFICATION_OPENED)) {
			values.put(DBConstant.EXTRA_NOTIFICATION_ID, bundle.getInt(JPushInterface.EXTRA_NOTIFICATION_ID));
			values.put(DBConstant.EXTRA_NOTIFICATION_TITLE, bundle.getString(JPushInterface.EXTRA_NOTIFICATION_TITLE));
			values.put(DBConstant.EXTRA_ALERT, bundle.getString(JPushInterface.EXTRA_ALERT));
			values.put(DBConstant.EXTRA_MSG_ID, bundle.getString(JPushInterface.EXTRA_MSG_ID));
			String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
			if (extras != null) {
				values.put(DBConstant.EXTRA_EXTRA, extras);
			}
		}

		// 用户接受Rich Push Javascript 回调函数的intent
		else if (action.equals(JPushInterface.ACTION_RICHPUSH_CALLBACK)) {
			String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
			if (extras != null) {
				values.put(DBConstant.EXTRA_EXTRA, extras);
			}
		}

		MLog.getIns().i("values = " + values.toString());

		// 向数据库中插入数据
		db.insert(DBConstant.TABLE_NAME, null, values);
	}

	/**
	 * 获取数据库intents表中所有Intent的ID
	 * 
	 * @return
	 */
	public static synchronized List<Integer> queryAllIntentsId(SQLiteDatabase db) {

		List<Integer> list = new ArrayList<Integer>();

		String querySQL = "select " + DBConstant.ID + " from " + DBConstant.TABLE_NAME;
		Cursor cursor = db.rawQuery(querySQL, null);
		if (cursor.moveToFirst()) {
			do {
				int intentId = cursor.getInt(cursor.getColumnIndex(DBConstant.ID));
				list.add(intentId);
			} while (cursor.moveToNext());
		}
		cursor.close();

		return list;
	}

	/**
	 * 根据Id获得Intent对象
	 * 
	 * @param id
	 * @return
	 */
	public static synchronized Intent getIntentById(SQLiteDatabase db, int id) {

		Bundle bundle = new Bundle();
		Intent intent = new Intent();
		intent.addCategory(MyReceiver.CATEGORY);

		// @formatter:off
		String querySQL = "select * from " + DBConstant.TABLE_NAME
						+ " where " + DBConstant.ID + " = " + id;
		// @formatter:on
		Cursor cursor = db.rawQuery(querySQL, null);
		if (cursor.moveToFirst()) {

			String action = cursor.getString(cursor.getColumnIndex(DBConstant.ACTION));
			if (action == null) {
				MLog.getIns().e("action == null");
				cursor.close();
				return null;
			}
			MLog.getIns().i("action = " + action);

			intent.setAction(action);

			// SDK 向 JPush Server 注册所得到的注册 ID
			if (action.equals(JPushInterface.ACTION_REGISTRATION_ID)) {
				bundle.putString(JPushInterface.EXTRA_REGISTRATION_ID, cursor.getString(cursor.getColumnIndex(DBConstant.EXTRA_REGISTRATION_ID)));
			}

			// 收到了自定义消息 Push
			else if (action.equals(JPushInterface.ACTION_MESSAGE_RECEIVED)) {
				bundle.putString(JPushInterface.EXTRA_TITLE, cursor.getString(cursor.getColumnIndex(DBConstant.EXTRA_TITLE)));
				bundle.putString(JPushInterface.EXTRA_MESSAGE, cursor.getString(cursor.getColumnIndex(DBConstant.EXTRA_MESSAGE)));
				bundle.putString(JPushInterface.EXTRA_CONTENT_TYPE, cursor.getString(cursor.getColumnIndex(DBConstant.EXTRA_CONTENT_TYPE)));
				bundle.putString(JPushInterface.EXTRA_RICHPUSH_FILE_PATH, cursor.getString(cursor.getColumnIndex(DBConstant.EXTRA_RICHPUSH_FILE_PATH)));
				bundle.putString(JPushInterface.EXTRA_MSG_ID, cursor.getString(cursor.getColumnIndex(DBConstant.EXTRA_MSG_ID)));
				String extras = cursor.getString(cursor.getColumnIndex(DBConstant.EXTRA_EXTRA));
				if (!extras.isEmpty()) {
					bundle.putString(JPushInterface.EXTRA_EXTRA, extras);
				}
			}

			// 收到了通知 Push
			else if (action.equals(JPushInterface.ACTION_NOTIFICATION_RECEIVED)) {
				bundle.putInt(JPushInterface.EXTRA_NOTIFICATION_ID, cursor.getInt(cursor.getColumnIndex(DBConstant.EXTRA_NOTIFICATION_ID)));
				bundle.putString(JPushInterface.EXTRA_NOTIFICATION_TITLE, cursor.getString(cursor.getColumnIndex(DBConstant.EXTRA_NOTIFICATION_TITLE)));
				bundle.putString(JPushInterface.EXTRA_ALERT, cursor.getString(cursor.getColumnIndex(DBConstant.EXTRA_ALERT)));
				bundle.putString(JPushInterface.EXTRA_CONTENT_TYPE, cursor.getString(cursor.getColumnIndex(DBConstant.EXTRA_CONTENT_TYPE)));
				bundle.putString(JPushInterface.EXTRA_RICHPUSH_HTML_PATH, cursor.getString(cursor.getColumnIndex(DBConstant.EXTRA_RICHPUSH_HTML_PATH)));
				bundle.putString(JPushInterface.EXTRA_RICHPUSH_HTML_RES, cursor.getString(cursor.getColumnIndex(DBConstant.EXTRA_RICHPUSH_HTML_RES)));
				bundle.putString(JPushInterface.EXTRA_MSG_ID, cursor.getString(cursor.getColumnIndex(DBConstant.EXTRA_MSG_ID)));
				String extras = cursor.getString(cursor.getColumnIndex(DBConstant.EXTRA_EXTRA));
				if (!extras.isEmpty()) {
					bundle.putString(JPushInterface.EXTRA_EXTRA, extras);
				}
			}

			// 用户点击了通知
			else if (action.equals(JPushInterface.ACTION_NOTIFICATION_OPENED)) {
				bundle.putInt(JPushInterface.EXTRA_NOTIFICATION_ID, cursor.getInt(cursor.getColumnIndex(DBConstant.EXTRA_NOTIFICATION_ID)));
				bundle.putString(JPushInterface.EXTRA_NOTIFICATION_TITLE, cursor.getString(cursor.getColumnIndex(DBConstant.EXTRA_NOTIFICATION_TITLE)));
				bundle.putString(JPushInterface.EXTRA_ALERT, cursor.getString(cursor.getColumnIndex(DBConstant.EXTRA_ALERT)));
				bundle.putString(JPushInterface.EXTRA_MSG_ID, cursor.getString(cursor.getColumnIndex(DBConstant.EXTRA_MSG_ID)));
				String extras = cursor.getString(cursor.getColumnIndex(DBConstant.EXTRA_EXTRA));
				if (!extras.isEmpty()) {
					bundle.putString(JPushInterface.EXTRA_EXTRA, extras);
				}
			}

			// 用户接受Rich Push Javascript 回调函数的intent
			else if (action.equals(JPushInterface.ACTION_RICHPUSH_CALLBACK)) {
				String extras = cursor.getString(cursor.getColumnIndex(DBConstant.EXTRA_EXTRA));
				if (!extras.isEmpty()) {
					bundle.putString(JPushInterface.EXTRA_EXTRA, extras);
				}
			}

		} else {
			cursor.close();
			return null;
		}
		cursor.close();
		intent.putExtras(bundle);

		return intent;
	}

	/**
	 * 删除数据库中所有的Intent
	 * 
	 * @param db
	 */
	public static synchronized void deleteAllIntents(SQLiteDatabase db) {

		String deleteSQL = "delete from " + DBConstant.TABLE_NAME;
		db.execSQL(deleteSQL);

	}
}
