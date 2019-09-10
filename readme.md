#### flutter_boost流程

在flutter中打开flutter或者原生

```dart
FlutterBoost.singleton.open("sample://nativePage", urlParams:{
  "query": {"aaa": "bbb"}
})
```

``` dart
  Future<Map<String,dynamic>> open(String url,{Map<String,dynamic> urlParams,Map<String,dynamic> exts}){

    Map<String, dynamic> properties = new Map<String, dynamic>();
    properties["url"] = url;
    properties["urlParams"] = urlParams;
    properties["exts"] = exts;
    return channel.invokeMethod<Map<String,dynamic>>(
        'openPage', properties);
  }
```

这里也是通过channel调用原生的方法，方法名字是**openPage**

在FlutterBoost.BoostMethodHandler中

``` java
case "openPage":
                {
                    try {
                        Map<String,Object> params = methodCall.argument("urlParams");
                        Map<String,Object> exts = methodCall.argument("exts");
                        String url = methodCall.argument("url");

                        mManager.openContainer(url, params, exts, new FlutterViewContainerManager.OnResult() {
                            @Override
                            public void onResult(Map<String, Object> rlt) {
                                if (result != null) {
                                    result.success(rlt);
                                }
                            }
                        });
                    }catch (Throwable t){
                        result.error("open page error",t.getMessage(),t);
                    }
                }
                break;
```

这里的mManager是`FlutterViewContainerManager`一个实例，`openContainer`方法的实现是这样的

``` java
    void openContainer(String url, Map<String, Object> urlParams, Map<String, Object> exts,OnResult onResult) {
        Context context = FlutterBoost.singleton().currentActivity();
        if(context == null) {
            context = FlutterBoost.singleton().platform().getApplication();
        }

        if(urlParams == null) {
            urlParams = new HashMap<>();
        }

        int requestCode = 0;
        final Object v = urlParams.remove("requestCode");
        if(v != null) {
            requestCode = Integer.valueOf(String.valueOf(v));
        }

        final String uniqueId = ContainerRecord.genUniqueId(url);
        urlParams.put(IContainerRecord.UNIQ_KEY,uniqueId);
        if(onResult != null) {
            mOnResults.put(uniqueId,onResult);
        }

        FlutterBoost.singleton().platform().openContainer(context,url,urlParams,requestCode,exts);
    }
```

注意最后一句`FlutterBoost.singleton().platform()`这里的`platform`返回的是 `IPlatform`类型，也就是我们在Application中初始化`FlutterBoost`时传入的对象

``` java
FlutterBoost.init(new Platform(){...})
```

最终调用了这里面的`openContainer`方法，将路由处理交给了原生。



``` mermaid

graph LR
	a --- b
```


