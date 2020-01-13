package org.zywx.wbpalmstar.widgetone.uexjpush;

import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.base.util.ActivityActionRecorder;
import org.zywx.wbpalmstar.engine.DataHelper;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.universalex.EUExBase;
import org.zywx.wbpalmstar.engine.universalex.EUExEventListener;
import org.zywx.wbpalmstar.widgetone.uexjpush.db.DBConstant;
import org.zywx.wbpalmstar.widgetone.uexjpush.db.DBFunction;
import org.zywx.wbpalmstar.widgetone.uexjpush.db.DBHelper;
import org.zywx.wbpalmstar.widgetone.uexjpush.receiver.MyReceiver;
import org.zywx.wbpalmstar.widgetone.uexjpush.utils.SharedPreferencesUtil;
import org.zywx.wbpalmstar.widgetone.uexjpush.vo.SetTagsResultVO;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;
import cn.jpush.android.data.JPushLocalNotification;

public class EUExJPush extends EUExBase implements CallBack {

    private static final String TAG = "EUExJPush";
    
    // 通知栏管理器
    private NotificationManager mNotificationManager;

    public EUExJPush(Context context, EBrowserView eBrowserView) {
        super(context, eBrowserView);
        // plugin.xml中声明了单例插件（global="true"），则此插件入口类实例只会初始化一次。
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        MyReceiver.setCallBack(this);
        registerAppEventListener(new EUExEventListener() {
            @Override
            public boolean onEvent(int event) {
                if (event == F_UEX_EVENT_TYPE_APP_ON_RESUME){
                    // 应用进入前台
                    Intent intent = new Intent();
                    intent.setAction(MyReceiver.BROADCAST_ON_APP_ENTER_FORGROUND);
                    intent.setPackage(mContext.getPackageName());
                    mContext.sendBroadcast(intent);
                }
                return false;
            }
        });
    }


    @Override
    protected boolean clean() {
        return false;
    }

    public static void onApplicationCreate(Context context) {
        // 初始化极光推送
        JPushInterface.setDebugMode(true);
        JPushInterface.init(context.getApplicationContext());
    }

    /**
     * 拦截onResume
     *
     * @param context
     */
    public static void onActivityResume(Context context) {

        // 初始化极光推送
//        JPushInterface.init(context.getApplicationContext());

        // 数据库操作
        if (MyReceiver.callBack != null) {

            // 获得数据库对象
            final Context contextFinal = context;
            DBHelper helper = new DBHelper(context, DBConstant.DB_NAME, null, 1);
            final SQLiteDatabase db = helper.getWritableDatabase();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    List<Integer> intentsIdList = DBFunction.queryAllIntentsId(db);
                    if (intentsIdList.size() == 0) {// 如果数据库中没有intent
                        return;
                    }
                    BDebug.d(TAG, "App未运行时，存在遗留未回调的数据，现在进行回调。");
                    for (int i = 0; i < intentsIdList.size(); i++) {
                        Intent intent = DBFunction.getIntentById(db, intentsIdList.get(i));// 从数据库中获得Intent
                        intent.setComponent(new ComponentName(contextFinal.getPackageName(),"org.zywx.wbpalmstar.widgetone.uexjpush.receiver.MyReceiver"));
                        intent.setPackage(contextFinal.getPackageName());
                        contextFinal.sendBroadcast(intent);// 发送广播
                    }
                    // 发送删除DB中所有Intent的广播
                    // 至于为什么不用handler，因为这里不能直接使用mHandler，传进来又太麻烦，所以直接发广播好了
                    Intent intent = new Intent();
                    intent.setAction(MyReceiver.BROADCAST_DELETE_ALL_INTENTS_IN_DB);
//                    intent.addCategory(MyReceiver.CATEGORY);
                    intent.setComponent(new ComponentName(contextFinal.getPackageName(),"org.zywx.wbpalmstar.widgetone.uexjpush.receiver.MyReceiver"));
                    intent.setPackage(contextFinal.getPackageName());
                    contextFinal.sendBroadcast(intent);
                }
            },500);//延时是为了能显示alert对话框

        }

    }

    /**
     * 配置项。例如：是否启用自动角标功能
     *
     * @param params
     */
    public void setConfig(String[] params) {
        if (params.length < 1){
            return;
        }
        String jsonStr = params[0];
        try {
            JSONObject json = new JSONObject(jsonStr);
            boolean isEnbaleBadge = json.optBoolean("isEnableBadge", false);
            SharedPreferencesUtil.saveBadgerConfig(mContext, isEnbaleBadge);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止推送服务
     *
     * @param params
     */
    public void stopPush(String[] params) {
        JPushInterface.stopPush(mContext.getApplicationContext());
    }


    /**
     * 恢复推送服务
     *
     * @param params
     */
    public void resumePush(String[] params) {
        JPushInterface.resumePush(mContext.getApplicationContext());
    }


    public void isPushStopped(String[] params) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("result", JPushInterface.isPushStopped(mContext.getApplicationContext()) ? 0 : 1);
            @SuppressWarnings("unused")
            boolean result = JPushInterface.isPushStopped(mContext.getApplicationContext());
            String data = jsonObject.toString();
            String js = SCRIPT_HEADER + "if(" + JsConst.CALLBACK_ISPUSHSTOPPED + "){" + JsConst.CALLBACK_ISPUSHSTOPPED + "('" + data + "');}";
            evaluateRootWindowScript(js);
        } catch (JSONException e) {
        }
    }

    public void setAliasAndTags(String[] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        String json = params[0];
        Set<String> tags = null;
        String alias = null;
        try {
            JSONObject jsonObject = new JSONObject(json);
            alias = jsonObject.getString("alias");
            JSONArray tagsArray = jsonObject.getJSONArray("tags");
            if (tagsArray != null) {
                tags = new HashSet<String>();
                for (int i = 0; i < tagsArray.length(); i++) {
                    tags.add(tagsArray.getString(i));
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        String str = null;
        if (params.length == 2) {
            str = params[1];
        }
        final String funcId = str;
        JPushInterface.setAliasAndTags(mContext.getApplicationContext(), alias, JPushInterface.filterValidTags(tags), new TagAliasCallback() {
            @Override
            public void gotResult(int i, String s, Set<String> set) {

                SetTagsResultVO resultVO = new SetTagsResultVO();
                resultVO.setResult(String.valueOf(i));
                resultVO.setAlias(s);
                if (set != null) {
                    List<String> tags = new ArrayList<String>();
                    tags.addAll(set);
                    resultVO.setTags(tags);
                }
                String result = DataHelper.gson.toJson(resultVO);
                if (funcId != null) {
                    try {
                        callbackToJs(Integer.parseInt(funcId), false, i, new JSONObject(result));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    String js = SCRIPT_HEADER + "if(" + JsConst.CALLBACK_SETALIASANDTAGS + "){" + JsConst.CALLBACK_SETALIASANDTAGS + "('" + result + "');}";
                    evaluateRootWindowScript(js);
                }
            }
        });
    }

    public void setAlias(String[] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        String json = params[0];
        String alias = null;
        try {
            JSONObject jsonObject = new JSONObject(json);
            alias = jsonObject.getString("alias");
        } catch (JSONException e) {
        }
        String str = null;
        if (params.length == 2) {
            str = params[1];
        }
        final String funcId = str;
        JPushInterface.setAlias(mContext.getApplicationContext(), alias, new TagAliasCallback() {
            @Override
            public void gotResult(int i, String s, Set<String> set) {
                SetTagsResultVO resultVO = new SetTagsResultVO();
                resultVO.setResult(String.valueOf(i));
                resultVO.setAlias(s);
                if (set != null) {
                    List<String> tags = new ArrayList<String>();
                    tags.addAll(set);
                    resultVO.setTags(tags);
                }
                String result = DataHelper.gson.toJson(resultVO);
                if (funcId != null) {
                    try {
                        callbackToJs(Integer.parseInt(funcId), false, i, new JSONObject(result));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    String js = SCRIPT_HEADER + "if(" + JsConst.CALLBACK_SETALIAS + "){" + JsConst.CALLBACK_SETALIAS + "('" + result + "');}";
                    evaluateRootWindowScript(js);
                }
            }
        });
    }

    public void setTags(String[] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        String json = params[0];
        Set<String> tags = new HashSet<String>();
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray("tags");
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    tags.add(jsonArray.getString(i));
                }
            }
        } catch (JSONException e) {
        }
        String str = null;
        if (params.length == 2) {
            str = params[1];
        }
        final String funcId = str;
        JPushInterface.setTags(mContext.getApplicationContext(), JPushInterface.filterValidTags(tags), new TagAliasCallback() {
            @Override
            public void gotResult(int i, String s, Set<String> set) {
                SetTagsResultVO resultVO = new SetTagsResultVO();
                resultVO.setResult(String.valueOf(i));
                resultVO.setAlias(s);
                if (set != null) {
                    List<String> tags = new ArrayList<String>();
                    tags.addAll(set);
                    resultVO.setTags(tags);
                }
                String result = DataHelper.gson.toJson(resultVO);
                if (funcId != null) {
                    try {
                        callbackToJs(Integer.parseInt(funcId), false, i, new JSONObject(result));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    String js = SCRIPT_HEADER + "if(" + JsConst.CALLBACK_SETTAGS + "){" + JsConst.CALLBACK_SETTAGS + "('" + result + "');}";
                    evaluateRootWindowScript(js);
                }
            }
        });
    }

    public String getRegistrationID(String[] params) {
        JSONObject jsonObject = new JSONObject();
        String id = JPushInterface.getRegistrationID(mContext.getApplicationContext());
        try {
            jsonObject.put("registrationID", id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String data = jsonObject.toString();
        String js = SCRIPT_HEADER + "if(" + JsConst.CALLBACK_GETREGISTRATIONID + "){" + JsConst.CALLBACK_GETREGISTRATIONID + "('" + data + "');}";
        evaluateRootWindowScript(js);
        return id;
    }

    public void reportNotificationOpened(String[] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        String json = params[0];
        String msgId = null;
        try {
            JSONObject jsonObject = new JSONObject(json);
            msgId = jsonObject.optString("msgId");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JPushInterface.reportNotificationOpened(mContext.getApplicationContext(), msgId);
    }

    public void clearAllNotifications(String[] params) {
        JPushInterface.clearAllNotifications(mContext.getApplicationContext());

    }

    public void clearNotificationById(String[] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        String json = params[0];
        int id = 0;
        try {
            JSONObject jsonObject = new JSONObject(json);
            id = jsonObject.getInt("notificationId");
            JPushInterface.clearNotificationById(mContext.getApplicationContext(), id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void setPushTime(String[] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        String json = params[0];
        Set<Integer> weekDays = new HashSet<Integer>();
        int startHour = 0;
        int endHour = 0;
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray("weekDays");
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    weekDays.add(jsonArray.getInt(i));
                }
            }
            startHour = jsonObject.getInt("startHour");
            endHour = jsonObject.getInt("endHour");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JPushInterface.setPushTime(mContext.getApplicationContext(), weekDays, startHour, endHour);
    }


    public void setSilenceTime(String[] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        String json = params[0];
        int startHour;
        int startMinute;
        int endHour;
        int endMinute;
        try {
            JSONObject jsonObject = new JSONObject(json);
            startHour = jsonObject.getInt("startHour");
            startMinute = jsonObject.getInt("startMinute");
            endHour = jsonObject.getInt("endHour");
            endMinute = jsonObject.getInt("endMinute");
            JPushInterface.setSilenceTime(mContext.getApplicationContext(), startHour, startMinute, endHour, endMinute);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setLatestNotificationNumber(String[] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        String json = params[0];
        int maxNum;
        try {
            JSONObject jsonObject = new JSONObject(json);
            maxNum = jsonObject.getInt("maxNum");
            JPushInterface.setLatestNotificationNumber(mContext.getApplicationContext(), maxNum);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getConnectionState(String[] params) {
        String funcId = null;
        if (null != params && params.length == 1) {
            funcId = params[0];
        }
        JSONObject jsonObject = new JSONObject();
        try {
            int result = JPushInterface.getConnectionState(mContext.getApplicationContext()) ? 0 : 1;
            jsonObject.put("result", result);
            String data = jsonObject.toString();
            if (null != funcId) {
                callbackToJs(Integer.parseInt(funcId), false, 0, result);
            } else {
                String js = SCRIPT_HEADER + "if(" + JsConst.CALLBACK_GETCONNECTIONSTATE + "){" + JsConst.CALLBACK_GETCONNECTIONSTATE + "('" + data + "');}";
                evaluateRootWindowScript(js);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void addLocalNotification(String[] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        String json = params[0];
        long builderId;
        String title;
        String content;
        String extras;
        long notificationId;
        long broadCastTime;
        System.currentTimeMillis();
        try {
            JSONObject jsonObject = new JSONObject(json);
            builderId = jsonObject.getLong("builderId");
            title = jsonObject.optString("title");
            content = jsonObject.optString("content");
            extras = jsonObject.optString("extras");
            notificationId = jsonObject.getLong("notificationId");
            broadCastTime = jsonObject.getLong("broadCastTime");
            JPushLocalNotification ln = new JPushLocalNotification();
            ln.setBuilderId(builderId);
            ln.setContent(content);
            ln.setTitle(title);
            ln.setNotificationId(notificationId);
            long atTime = System.currentTimeMillis() + broadCastTime;
            ln.setBroadcastTime(atTime);
            ln.setExtras(extras);
            JPushInterface.addLocalNotification(mContext.getApplicationContext(), ln);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void removeLocalNotification(String[] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        String json = params[0];
        long notificationId;
        try {
            JSONObject jsonObject = new JSONObject(json);
            notificationId = jsonObject.getLong("notificationId");
            JPushInterface.removeLocalNotification(mContext.getApplicationContext(), notificationId);
            if (mNotificationManager != null) {
                mNotificationManager.cancel((int) notificationId);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void clearLocalNotifications(String[] params) {
        JPushInterface.clearLocalNotifications(mContext.getApplicationContext());
        if (mNotificationManager != null) {
            mNotificationManager.cancelAll();
        }
    }

    @Override
    public void onReceiveRegistration(String jsonData) {
        String js = SCRIPT_HEADER + "if(" + JsConst.ONRECEIVEREGISTRATION + "){" + JsConst.ONRECEIVEREGISTRATION + "('" + jsonData + "');}";
        evaluateRootWindowScript(js);
    }

    @Override
    public void onReceiveMessage(String jsonData) {
        String js = SCRIPT_HEADER + "if(" + JsConst.ONRECEIVEMESSAGE + "){" + JsConst.ONRECEIVEMESSAGE + "('" + jsonData + "');}";
        evaluateRootWindowScript(js);
    }

    @Override
    public void onReceiveNotification(String jsonData) {
        String js = SCRIPT_HEADER + "if(" + JsConst.ONRECEIVENOTIFICATION + "){" + JsConst.ONRECEIVENOTIFICATION + "('" + jsonData + "');}";
        evaluateRootWindowScript(js);
    }

    @Override
    public void onReceiveNotificationOpen(String jsonData) {
        BDebug.e("AppCan:", "[AppCan]MyReceiver点击用户自定义处理广播onReceiveNotificationOpen："+jsonData);
        final String js = SCRIPT_HEADER + "if(" + JsConst.ONRECEIVENOTIFICATIONOPEN + "){" + JsConst.ONRECEIVENOTIFICATIONOPEN + "('" + jsonData + "');}";
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                evaluateRootWindowScript(js);
            }
        },2800);
    }

    @Override
    public void onReceiveConnectionChange(String jsonData) {
        String js = SCRIPT_HEADER + "if(" + JsConst.ONRECEIVECONNECTIONCHANGE + "){" + JsConst.ONRECEIVECONNECTIONCHANGE + "('" + jsonData + "');}";
        evaluateRootWindowScript(js);
    }

    /**
     * 执行Root Window脚本
     *
     * @param script
     */
    private void evaluateRootWindowScript(String script) {
        BDebug.i(script);
        evaluateScript("root", 0, script);
    }
}
