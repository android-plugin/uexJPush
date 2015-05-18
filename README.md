#uexJPush插件接口文档

####接口定义
1.初始化

```
init（param）
var param={
	debug://是否开启debug，默认关闭，0-关闭，1-开启
};
```
2.停止推送服务

```
stopPush ();
```
3.恢复推送服务

```
resumePush ();
```
4.用来检查 Push Service 是否已经被停止

```
isPushStopped ();
```
5.isPushStopped回调

```
cbIsPushStopped（param）
var param={
	result://0-关闭，1-开启
};
```
6.SDK 向 JPush Server 注册所得到的注册 ID 

```
onReceiveRegistration（param）
var param={
	title://SDK 向 JPush Server 注册所得到的注册 全局唯一的 ID ，可以通过此 ID 向对应的	 			 //客户端发送消息和通知
};
```
7.收到了自定义消息 Push 

```
onReceiveMessage（param）
var param={
	title:,//服务器推送下来的消息的标题
	message:,//对应 Portal 推送消息界面上的"自定义消息内容”字段
	extras:,//对应 Portal 推送消息界面上的“可选设置”里的附加字段
	type:,//对应 API 消息内容的 content_type 字段
	file:,//富媒体通消息推送下载后的文件路径和文件名
	msgId://唯一标识消息的 ID, 可用于上报统计等
};
```
8.收到了通知 Push 

```
onReceiveNotification（param）
var param={
	title:,//对应 API 通知内容的 n_title 字段。对应 Portal 推送通知界面上的“通知标题”字段
	content:,//对应 API 通知内容的 n_content 字段。对应 Portal 推送通知界面上的“通知内容”字段。
	extras:,//对应 API 通知内容的 n_extras 字段。对应 Portal 推送消息界面上的“可选设置”里的附加字段。
	notificationId:,//通知栏的Notification ID，可以用于清除Notification
	type:,//对应 API 消息内容的 content_type 字段。Portal 上暂时未提供输入字段。
	fileHtml:,//富媒体通知推送下载的HTML的文件路径,用于展现WebView
	fileStr:,//富媒体通知推送下载的图片资源的文件名,多个文件名用 “,” 分开。 与 “JPushInterface.EXTRA_RICHPUSH_HTML_PATH” 位于同一个路径。
	msgId://唯一标识通知消息的 ID, 可用于上报统计等
};
```
9.用户点击了通知

```
onReceiveNotificationOpen（param）
var param={
	title:,//对应 API 通知内容的 n_title 字段。对应 Portal 推送通知界面上的“通知标题”字段
	content:,//对应 API 通知内容的 n_content 字段。对应 Portal 推送通知界面上的“通知内容”字段。
	extras:,//对应 API 通知内容的 n_extras 字段。对应 Portal 推送消息界面上的“可选设置”里的附加字段。
	notificationId:,//通知栏的Notification ID，可以用于清除Notification
	msgId://唯一标识通知消息的 ID, 可用于上报统计等
};
```
10.同时设置别名与标签

```
setAliasAndTags（param）
var param={
	alias:, //不传 此次调用不设置此值。
			//"" （空字符串）表示取消之前的设置。
			//每次调用设置有效的别名，覆盖之前的设置。
			//有效的别名组成：字母（区分大小写）、数字、下划线、汉字。
			//限制：alias 命名长度限制为 40 字节。（判断长度需采用UTF-8编码）
	tags:,  //不传 此次调用不设置此值。
			//空数组或列表表示取消之前的设置。
			//每次调用至少设置一个 tag，覆盖之前的设置，不是新增。
			//有效的标签组成：字母（区分大小写）、数字、下划线、汉字。
			//限制：每个 tag 命名长度限制为 40 字节，最多支持设置 100 个 tag，但总长度不得超过1K字节。（判断长度需采用UTF-8编码）
};
			//Set<String>类型
```
11.setAliasAndTags回调

```
cbSetAliasAndTags（param）
var param={
	result://0-成功,其他见表格
	alias://原设置的别名
	tags://原设置的标签
};
```
|result|描述|详细解释|
|------|---|------|
|6001|无效的设置，tag/alias 不应参数都为 null	
|6002|	设置超时	|建议重试
|6003|	alias 字符串不合法	|有效的别名、标签组成：字母（区分大小写）、数字、下划线、汉字。
|6004|	alias超长。最多 40个字节	|中文 UTF-8 是 3 个字节
|6005|	某一个 tag 字符串不合法	|有效的别名、标签组成：字母（区分大小写）、数字、下划线、汉字。
|6006|	某一个 tag 超长。一个 tag 最多 40个字节	|中文 UTF-8 是 3 个字节
|6007|	tags 数量超出限制。最多 100个	|这是一台设备的限制。一个应用全局的标签数量无限制。
|6008|	tag/alias 超出总长度限制	|总长度最多 1K 字节
|6011|	10s内设置tag或alias大于10次|	短时间内操作过于频繁


12.设置别名

```
setAlias（param）
var param={
	alias:, //"" （空字符串）表示取消之前的设置。
			//每次调用设置有效的别名，覆盖之前的设置。
			//有效的别名组成：字母（区分大小写）、数字、下划线、汉字。
			//限制：alias 命名长度限制为 40 字节。（判断长度需采用UTF-8编码）
};
```
13.setAlias回调

```
cbSetAlias（param）
var param={
	result://0-成功,其他见表格
	alias://原设置的别名
	tags://原设置的标签
		 //Set<String>类型
};
```
14.设置标签

```
setTags（param）
var param={
	tags:,  //空数组或列表表示取消之前的设置。
			//每次调用至少设置一个 tag，覆盖之前的设置，不是新增。
			//有效的标签组成：字母（区分大小写）、数字、下划线、汉字。
			//限制：每个 tag 命名长度限制为 40 字节，最多支持设置 100 个 tag，但总长度不得超过1K字节。（判断长度需采用UTF-8编码）
			//Set<String>类型
};
```
15.setTags回调

```
cbSetTags（param）
var param={
	result://0-成功,其他见表格
	alias://原设置的别名
	tags://原设置的标签
};
```
16.取得应用程序对应的 RegistrationID

```
getRegistrationID（);
```
17.getRegistrationID回调

```
cbGetRegistrationID（param）
var param={
	registrationID://
};
```
18.用于上报用户的通知栏被打开，或者用于上报用户自定义消息被展示等客户端需要统计的事件

```
reportNotificationOpened（param）
var param={
	msgId://
};
```
19.清除通知

```
clearAllNotifications（）;
```
20.清除通知

```
clearNotificationById（param）
var param={
	int notificationId：通知ID
};
```
21.设置允许推送时间 API

```
setPushTime（param）
var param={
	weekDays:,//Set<Integer>  0表示星期天，1表示星期一，以此类推。 （7天制，Set集合里面的int范围为0到6）
	startHour:,//Integer 允许推送的开始时间 （24小时制：startHour的范围为0到23）
	endHour://Integer 允许推送的结束时间
};
```
22.设置通知静默时间 如果在该时间段内收到消息，则：不会有铃声和震动

```
setSilenceTime（param）
var param={
	startHour:,//Integer 静音时段的开始时间 - 小时 （24小时制，范围：0~23 ）
	startMinute:,//Integer 静音时段的开始时间 - 分钟（范围：0~59 ）
	endHour:,//Integer 静音时段的结束时间 - 小时 （24小时制，范围：0~23 ）
	endMinute://Integer 静音时段的结束时间 - 分钟（范围：0~59 ）
};
```
23.设置保留最近通知条数

```
setLatestNotificationNumber（param）
var param={
	maxNum:,//Integer 最多显示的条数
};
```
24 获取推送连接状态

```
getConnectionState（）;
```
25 getConnectionState 回调

```
cbGetConnectionState（param）
var param={
	result://0-已连接上，1-未连接
};
```
26 连接状态变化

```
onReceiveConnectionChange（param）
var param={
	connect:,//0-已连接上，1-未连接
};
```
27 添加一个本地通知

```
addLocalNotification（param）
var param={
	builderId:,//long 设置本地通知样式
	title:,//本地通知的title
	content:,//设置本地通知的content
	extras:,//额外的数据信息extras为json字符串
	notificationId:,//int 设置本地通知的ID
	broadCastTime:,//long 设置本地通知延迟触发时间，毫秒为单位，如设置10000为延迟10秒添加通知
};
```
28 移除一个本地通知

```
removeLocalNotification（param）
var param={
	notificationId://int 通知id
};
```
29 移除所有的通知

```
clearLocalNotifications（);
```

#### Android插件配置说明

插件需要在`AndroidManifest.xml`中查找替换所有的`org.zywx.wbpalmstar.widgetone.uexJPushDemo`改为自己的包名。

并将`<meta-data android:name="JPUSH_APPKEY" android:value=""/>`中的value替换为自己在极光推送申请的appkey
