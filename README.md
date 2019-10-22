API文档
-------------------------------------------------------------
  * API - [https://github.com/AppCanOpenSource/appcan-docs](https://github.com/AppCanOpenSource/appcan-docs)
  *
  #### Android
  #### **附加 极光推送SDK中集成小米华为推送，需要配置信息**

示例配置代码如下:

```xml
 <config desc="uexJPush" type="KEY">
    <param name="$UEXJPUSH_PACKAGE$" platform="Android" value="org.zywx.wbpalmstar.widgetone.uexSZPortal"/>
    <param name="$UEXJPUSH_APPKEY$" platform="Android" value="7d2a5fee085a5aeabec706e5"/>
    <!--小米推送通道配置-->
    <param name="$UEXMIXIAOMI_APPID$" platform="Android" value="MI-2882303761517843561"/>
    <param name="$UEXMIXIAOMI_APPKEY$" platform="Android" value="MI-5871784334561"/>
    <!--华为推送通道配置-->
    <param name="$UEXHUAWEIAPPID$" platform="Android" value="appid=100363851"/>
  </config>
```
**只需修改value的值**即可完成相应key的配置,其中value值即可
