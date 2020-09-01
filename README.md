API文档
-------------------------------------------------------------
  * API - [https://github.com/AppCanOpenSource/appcan-docs](https://github.com/AppCanOpenSource/appcan-docs)
  *
  #### Android
  #### **附加 极光推送SDK中集成小米华为魅族OPPO等厂商推送，需要配置信息（在config.xml文件中）**

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
