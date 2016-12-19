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
import cn.jpush.android.api.JPushInterface;
import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.widgetone.uexjpush.db.DBConstant;
import org.zywx.wbpalmstar.widgetone.uexjpush.db.DBFunction;
import org.zywx.wbpalmstar.widgetone.uexjpush.db.DBHelper;

import java.util.List;

/**
 * 中转Receiver
 * <p>
 * 这个Receiver将在uexjpush进程中
 * <p>
 * 接收到极光推送的广播后启动App进程，并将广播的Intent发给MyReceiver
 *
 * @author waka
 * @version createTime:2016年5月5日 下午6:16:38
 */
public class TransitReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

        BDebug.d("action = " , intent.getAction());

        // 如果是点击广播，尝试启动App
        if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {

            if (!isAppForground(context)) {// 如果App不在前台

                runApp(context);// 启动App

                // boolean result =
                // SharedPreferencesUtil.saveIntent(context.getApplicationContext(),
                // intent);// 将Intent放在SharedPreferences中
                // BDebug.d("将Intent放在SharedPreferences的结果 = " + result);
            }
        }
        BDebug.d("转发Intent");

        /**
         * 转发广播
         */
        // 因为如果重新发原来的Intent的话,不论action或者category改成什么，
        // 还是会被当前的广播接收器收到,这样就达不到转发Intent的效果了
        // 所以新建一个Intent,action和bundle使用原来的,换一个category,使当前广播接收器接收不到
        Intent intent2 = new Intent();
        intent2.setAction(intent.getAction());
        intent2.putExtras(intent.getExtras());
        intent2.addCategory(MyReceiver.CATEGORY);

        // 发送新的广播
        context.sendBroadcast(intent2);

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
    public void runApp(Context context) {
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
            if (BDebug.DEBUG) {
                e.printStackTrace();
            }
        }
    }

}
