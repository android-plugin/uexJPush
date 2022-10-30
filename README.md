## API文档
-------------------------------------------------------------
  * API - [https://github.com/AppCanOpenSource/appcan-docs](https://github.com/AppCanOpenSource/appcan-docs)
  *
### Android配置说明
#### **配置1： 极光推送SDK中集成小米华为魅族OPPO等厂商推送，需要配置信息（在config.xml文件中）**

示例配置代码如下:

```xml
 <config desc="uexJPush" type="KEY">
    <!--本应用的包名ID-->
    <param name="$UEXJPUSH_PACKAGE$" platform="Android" value="org.zywx.wbpalmstar.widgetone.uexJPushDemo"/>
    <!--极光推送appkey-->
    <param name="$UEXJPUSH_APPKEY$" platform="Android" value="e905af7390a3413439d24377"/>
    <!--小米推送通道配置（不要遗漏前缀 MI-）-->
    <param name="$UEXMIXIAOMI_APPID$" platform="Android" value="MI-1234512345123451234"/>
    <param name="$UEXMIXIAOMI_APPKEY$" platform="Android" value="MI-1234512345123"/>
    <!--华为推送通道配置（不要遗漏前缀 appid=）-->
    <param name="$UEXHUAWEIAPPID$" platform="Android" value="appid=123451234"/>
    <!--魅族推送通道配置（不要遗漏前缀 MZ-）-->
    <param name="$UEXMEIZU_APPID$" platform="Android" value="MZ-12345"/>
    <param name="$UEXMEIZU_APPKEY$" platform="Android" value="MZ-12345"/>
    <!--OPPO推送通道配置（不要遗漏前缀 OP-）-->
    <param name="$UEXOPPO_APPID$" platform="Android" value="OP-12345"/>
    <param name="$UEXOPPO_APPKEY$" platform="Android" value="OP-12345"/>
    <param name="$UEXOPPO_APPSECRET$" platform="Android" value="OP-12345"/>
    <!--VIVO推送通道配置-->
    <param name="$UEXVIVO_APPID$" platform="Android" value="12345"/>
    <param name="$UEXVIVO_APPKEY$" platform="Android" value="12345"/>
  </config>
```
**只需修改value的值**即可完成相应key的配置,其中value值即可

#### **配置2： 服务端发送极光推送消息时，需要增加自定义入口参数**

不管是使用java还是node或者其他什么极光的服务端SDK，在对接时都会有个可以传入自定义入口类的参数，下面以node为例：

```

var androidObj = JPush.android(alert, title, 1, extras);
        androidObj.android['uri_activity'] = 'org.zywx.wbpalmstar.widgetone.uexjpush.activity.PushNotificationLoadingActivity';
        androidObj.android['uri_action'] = 'org.zywx.wbpalmstar.widgetone.uexjpush.activity.PushNotificationLoadingActivity';

```

重点在于，将uri_activity和uri_action两个参数都传入**org.zywx.wbpalmstar.widgetone.uexjpush.activity.PushNotificationLoadingActivity**，这个参数为uexJPush插件的接收厂商推送消息的入口类。

### 版本重要更新说明

4.4.15.极光推送SDK升级到4.8.4-google-play版本，并增加手动初始化的方式。

此版本更新后，下面的方法必须调用，即使你的App不需要申请隐私权限的认证，那也需要先执行一下下面的方法示例，否则极光推送SDK不会主动进行初始化，推送也不会生效。

uexJPush.setConfig方法增加了一个参数isUserConfirmPrivacy，用于告知插件用户是否已经同意了隐私策略，防止插件过早进行初始化操作导致错误的申请权限时机。

具体使用方法是：当用户点击隐私策略的同意之后，执行下面的代码：
```javascript
function setConfig(){
    // 增加isUserConfirmPrivacy参数。开发者应该在用户同意隐私权限声明之后，调用此接口，此参数传true。
    // 此时插件会记录此状态，并进行极光SDK的初始化操作。此后app再次重启时，插件会根据上次本接口的调用状态来决定是否会在app启动时自动初始化
    // （此后该参数不会再有实际效果）
    var params = {
        isUserConfirmPrivacy: true
    };
    var data = JSON.stringify(params);
    uexJPush.setConfig(data);
}
```

