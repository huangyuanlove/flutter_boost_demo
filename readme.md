## 集成Flutter_boost
flutter_boost 地址：https://github.com/alibaba/flutter_boost
集成之后的项目地址：https://github.com/huangyuanlove/flutter_boost_demo
flutter版本： v1.5.4-hotfix.2
flutter_boost版本：0.1.52

#### 集成过程
前提：**项目已经集成flutter，并且可以运行**

##### 添加依赖

根据官方说法：
打开pubspec.yaml并将以下行添加到依赖项：
```yaml
flutter_boost: ^0.1.52
```
或者可以直接依赖github的项目的版本，Tag，pub发布会有延迟，推荐直接依赖Github项目

```yaml

flutter_boost:
        git:
            url: 'https://github.com/alibaba/flutter_boost.git'
            ref: '0.1.52'
            
```
然后 `flutter packages get`一下

在主工程的build.gradle中依赖一下
`implementation project(path: ':flutter_boost')`,官方没有提这个，但是我在项目中不添加这个依赖，找不到对应的类


##### 在flutter_nodule侧
在`main.dart`中注册一下路由，过程和使用命名路由相似


``` dart

class _MyAppState extends State<MyApp> {
  @override
  void initState() {
    super.initState();

    FlutterBoost.singleton.registerPageBuilders({
      'first': (pageName, params, _) => FirstRouteWidget(),
      'second': (pageName, params, _) => SecondRouteWidget(params),
      'tab': (pageName, params, _) => TabRouteWidget(),
      'flutterFragment': (pageName, params, _) => FragmentRouteWidget(params),

      ///可以在native层通过 getContainerParams 来传递参数
      'flutterPage': (pageName, params, _) {
        print("flutterPage params:$params");

        return FlutterRouteWidget();
      },
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
        title: 'Flutter Boost example',
        builder: FlutterBoost.init(postPush: _onRoutePushed),
        home: Container());
  }

  void _onRoutePushed(
      String pageName, String uniqueId, Map params, Route route, Future _) {
  }
  
```
其中 `FirstRouteWidget`、`SecondRouteWidget` 、`TabRouteWidget`、`FragmentRouteWidget`、`FlutterRouteWidget`代码可以在`simple_page_widgets.dart`中找到

##### 在原生Android侧
修改`Application`继承`FlutterApplication`,并且在`onCreate`初始化`FlutterBoost`
``` java
        FlutterBoost.init(new Platform() {

            @Override
            public Application getApplication() {
                return MyApplication.this;
            }

            @Override
            public boolean isDebug() {
                return true;
            }

            @Override
            public void openContainer(Context context, String url, Map<String, Object> urlParams, int requestCode, Map<String, Object> exts) {
                //在flutter中调用FlutterBoost.singleton.open()方法，最终会走到这里进行处理
                PageRouter.openPageByUrl(context,url,urlParams,requestCode);
            }

            @Override
            public IFlutterEngineProvider engineProvider() {
                return new BoostEngineProvider(){
                    @Override
                    public BoostFlutterEngine createEngine(Context context) {
                        return new BoostFlutterEngine(context, new DartExecutor.DartEntrypoint(
                                context.getResources().getAssets(),
                                FlutterMain.findAppBundlePath(context),
                                "main"),"/");
                    }
                };
            }

            @Override
            public int whenEngineStart() {
                return ANY_ACTIVITY_CREATED;
            }

            @Override
            public void registerPlugins(PluginRegistry registry) {
                super.registerPlugins(registry);
            }
        });
```
Android侧包含flutter_view的容器，比如Activity需要继承`BoostFlutterActivity`，并实现`getContainerUrl()`和`getContainerUrlParams()`方法
其中`getContainerUrl`方法返回的值就是在`main.dart`中注册的路由，`getContainerUrlParams`方法返回值则是对应的`params`。

#### 尝试
目前的做法是在Android这边配置打开的协议，和iOS统一，打开页面之后传递的参数全部附加在Uri上，
比如在flutter中的调用打开另外一个flutter界面，第一个参数uri就是`sample://flutter/launch`,如果是打开native页面，则是`sample://native/launch`
这样两端各自处理各自的逻辑就好了。
在Android端， `PageRouter`类用来处理native和flutter页面互相打开的逻辑，将传递进来的参数拼接成Uri的形式，通过Intent.setData()方式传递到下一个页面，而不用关系是打开native还是flutter，
在FlutterView的容器`FlutterPageActivity`中，我们通过解析Uri，还原一下要打开的flutter页面的router以及需要的参数，通过上面说的两个方法传递给flutter。


#### flutter_boost流程

##### 在flutter中打开flutter或者原生

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
        。。。
        FlutterBoost.singleton().platform().openContainer(context,url,urlParams,requestCode,exts);
    }
```

注意最后一句`FlutterBoost.singleton().platform()`这里的`platform`返回的是 `IPlatform`类型，也就是我们在Application中初始化`FlutterBoost`时传入的对象

``` java
FlutterBoost.init(new Platform(){...})
```

最终调用了这里面的`openContainer`方法，将路由处理交给了原生。


##### 在flutter中关闭页面

调用

```dart
BoostContainerSettings settings = BoostContainer.of(context).settings;
FlutterBoost.singleton.close(settings.uniqueId,result: {"result":"data from second"});
```
在上面的flutter打开原生或者flutter页面时传的map参数，打下断点或者输出一下，可以看到里面会有一个`__container_uniqueid_key__`,
这个key是flutter_boost用来标示flutterview的容器，当我们需要在flutter中关闭页面的时候，需要传入这个key。
而关闭页面的时候
``` java
case "closePage":
            {
                try {
                    String uniqueId = methodCall.argument("uniqueId");
                    Map<String,Object> resultData = methodCall.argument("result");
                    Map<String,Object> exts = methodCall.argument("exts");

                    mManager.closeContainer(uniqueId, resultData,exts);
                    result.success(true);
                }catch (Throwable t){
                    result.error("close page error",t.getMessage(),t);
                }
            }
```
也是通过`FlutterViewContainerManager`的实例进行关闭
``` java
IContainerRecord closeContainer(String uniqueId, Map<String, Object> result,Map<String,Object> exts) {
    IContainerRecord targetRecord = null;
    for (Map.Entry<IFlutterViewContainer, IContainerRecord> entry : mRecordMap.entrySet()) {
        if (TextUtils.equals(uniqueId, entry.getValue().uniqueId())) {
            targetRecord = entry.getValue();
            break;
        }
    }

    if(targetRecord == null) {
        Debuger.exception("closeContainer can not find uniqueId:" + uniqueId);
    }

    FlutterBoost.singleton().platform().closeContainer(targetRecord,result,exts);
    return targetRecord;
}
```
调用FlutterBoost.singleton().platform()的关闭容器方法
``` java
@Override
public void closeContainer(IContainerRecord record, Map<String, Object> result, Map<String, Object> exts) {
    if(record == null) return;

    record.getContainer().finishContainer(result);
}
```
最终还是调用的record中container的finish方法。
而在ContainerRecord类(IContainerRecord的实现类)的onAppear方法中，会将当前页面放入自己维护的栈中。
具体执行顺序：
当打开FlutterView的容器(这里是我们自己继承自`BoostFlutterActivity`的类，实现了`finishContainer()`方法)，在`BoostFlutterActivity.onCreate()`中，调用
` FlutterBoost.singleton().containerManager().generateSyncer(this);`,将当前的容器放在`FlutterViewContainerManager`中，并且记录上面讲到唯一标示
当关闭容器的时候，则从里面查找到对应的容器，执行finishContainer()。


``` mermaid

graph LR
	a --- b
```


