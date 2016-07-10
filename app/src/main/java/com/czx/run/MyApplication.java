package com.czx.run;

import android.app.Application;
//import android.content.Context;
//
//import com.squareup.leakcanary.LeakCanary;
//import com.squareup.leakcanary.RefWatcher;

/**
 * Created by czx on 2016/7/4.
 */
public class MyApplication extends Application {

//    public static RefWatcher getRefWatcher(Context context) {
//        MyApplication application = (MyApplication)   context.getApplicationContext();
//        return application.mRefWatcher;
//    }
//
//    private RefWatcher mRefWatcher;

    @Override
    public void onCreate() {
        super.onCreate();
//        mRefWatcher = LeakCanary.install(this);
    }
}
