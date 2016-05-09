package org.zywx.wbpalmstar.widgetone.uexjpush.db;

import org.zywx.wbpalmstar.widgetone.uexjpush.utils.MLog;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 数据库辅助类
 * 
 * @author waka
 * @version createTime:2016年5月7日 下午4:00:47
 */
public class DBHelper extends SQLiteOpenHelper {

	@SuppressWarnings("unused")
	private Context mContext;

	// 创建Intents表SQL语句
	// @formatter:off
	private static final String CREATE_INTENTS_TABLE = 
			"create table " + DBConstant.TABLE_NAME + " ("
		  + DBConstant.ID + " integer primary key autoincrement, "	// intent id 自己加的，用于区分
		  + DBConstant.ACTION + " text, "								// intent的action
		  + DBConstant.EXTRA_REGISTRATION_ID + " text, "				
		  + DBConstant.EXTRA_TITLE + " text, "				
		  + DBConstant.EXTRA_MESSAGE + " text, "				
		  + DBConstant.EXTRA_CONTENT_TYPE + " text, "				
		  + DBConstant.EXTRA_RICHPUSH_FILE_PATH + " text, "				
		  + DBConstant.EXTRA_MSG_ID + " text, "				
		  + DBConstant.EXTRA_EXTRA + " text, "				
		  + DBConstant.EXTRA_NOTIFICATION_ID + " integer, "				
		  + DBConstant.EXTRA_NOTIFICATION_TITLE + " text, "				
		  + DBConstant.EXTRA_ALERT + " text, "				
		  + DBConstant.EXTRA_RICHPUSH_HTML_PATH + " text, "				
		  + DBConstant.EXTRA_RICHPUSH_HTML_RES + " text)"				
		  ;
	// @formatter:on

	/**
	 * 构造方法
	 * 
	 * @param context
	 * @param name
	 * @param factory
	 * @param version
	 */
	public DBHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
		mContext = context;
	}

	/**
	 * 创建数据库
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {

		MLog.getIns().i("start");

		db.execSQL(CREATE_INTENTS_TABLE);
		MLog.getIns().i("创建Intents数据表成功");
		// Toast.makeText(mContext, "创建Intents数据表成功",
		// Toast.LENGTH_SHORT).show();
	}

	/**
	 * 更新数据库
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		MLog.getIns().i("start");

	}

}
