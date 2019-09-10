package com.huangyuanlove.flutterboostdemo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

public class PageRouter {

    public static final String NATIVE_PAGE_URL = "sample://native/launch";
    public static final String FLUTTER_PAGE_URL = "sample://flutter/launch";
    public static final String FLUTTER_FRAGMENT_PAGE_URL = "sample://flutterFragmentPage";

    public static boolean openPageByUrl(Context context, String url, Map<String, Object> params) {
        return openPageByUrl(context, url, params, 0);
    }

    public static boolean openPageByUrl(Context context, String url, Map<String, Object> params, int requestCode) {
        try {
            Uri uri = Uri.parse(url);
            if(uri!=null){
                if(params!=null && params.size()>0){
                    for(Map.Entry<String,Object> entry : params.entrySet()){
                       uri= uri.buildUpon().appendQueryParameter(entry.getKey(),entry.getValue().toString()).build();
                    }
                }


                Intent intent = new Intent();
                intent.setData(uri);
                context.startActivity(intent);
                return true;
            }






            if (url.startsWith(FLUTTER_PAGE_URL)) {
                context.startActivity(new Intent(context, FlutterPageActivity.class));
                return true;
            } else if (url.startsWith(FLUTTER_FRAGMENT_PAGE_URL)) {
                context.startActivity(new Intent(context, FlutterFragmentPageActivity.class));
                return true;
            } else if (url.startsWith(NATIVE_PAGE_URL)) {
                context.startActivity(new Intent(context, NativePageActivity.class));
                return true;
            } else {
                return false;
            }
        } catch (Throwable t) {
            return false;
        }
    }
}
