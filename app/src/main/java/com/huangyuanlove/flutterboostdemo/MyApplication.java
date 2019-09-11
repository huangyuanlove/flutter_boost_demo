package com.huangyuanlove.flutterboostdemo;

import android.app.Application;
import android.content.Context;

import com.idlefish.flutterboost.BoostEngineProvider;
import com.idlefish.flutterboost.BoostFlutterEngine;
import com.idlefish.flutterboost.FlutterBoost;
import com.idlefish.flutterboost.Platform;
import com.idlefish.flutterboost.interfaces.IFlutterEngineProvider;

import java.util.Map;

import io.flutter.app.FlutterApplication;
import io.flutter.embedding.engine.dart.DartExecutor;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.view.FlutterMain;

/**
 * Description:
 * Author: huangyuan
 * Create on: 2019-09-09
 * Email: huangyuan@chunyu.me
 */
public class MyApplication extends FlutterApplication {

    @Override
    public void onCreate() {
        super.onCreate();

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
    }

}
