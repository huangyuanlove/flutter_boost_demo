package com.huangyuanlove.flutterboostdemo;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.idlefish.flutterboost.containers.BoostFlutterActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FlutterPageActivity extends BoostFlutterActivity {


    private String route;
    private Map<String, String> params;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("FlutterPageActivity", "onCreate");
        if (getIntent() != null) {
            Uri uri = getIntent().getData();
            if (uri != null) {
                route = uri.getQueryParameter("route");
                params = new HashMap<>();
                Set<String> parameterNames = uri.getQueryParameterNames();
                for (String name : parameterNames) {
                    params.put(name, uri.getQueryParameter(name));
                }
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("FlutterPageActivity", "onDestroy");
    }

    /**
     * 该方法返回当前Activity在Flutter层对应的name，
     * 混合栈将会在flutter层根据这个名字，在注册的Route表中查找对应的Widget
     * <p>
     * 在flutter层有注册函数：
     * FlutterBoost.singleton.registerPageBuilders({
     * 'first': (pageName, params, _) => FirstRouteWidget(),
     * 'second': (pageName, params, _) => SecondRouteWidget(),
     * ...
     * });
     * <p>
     * 该方法中返回的就是注册的key：first , second
     *
     * @return
     */
    @Override
    public String getContainerUrl() {
        Log.e("FlutterPageActivity", "getContainerUrl");
        if (TextUtils.isEmpty(route)) {
            return "flutterPage";
        }
        return route;
    }

    /**
     * 该方法返回的参数将会传递给上层的flutter对应的Widget
     * <p>
     * 在flutter层有注册函数：
     * FlutterBoost.singleton.registerPageBuilders({
     * 'first': (pageName, params, _) => FirstRouteWidget(),
     * 'second': (pageName, params, _) => SecondRouteWidget(),
     * ...
     * });
     * <p>
     * 该方法返回的参数就会封装成上面的params
     *
     * @return
     */
    @Override
    public Map getContainerUrlParams() {
        Log.e("FlutterPageActivity", "getContainerUrlParams");

        return params;
    }
}
