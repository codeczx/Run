package com.czx.run.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.autonavi.amap.mapcore.ConnectionManager;

/**
 * Created by czx on 2016/7/11.
 */
public class AccessUtils {

    public static boolean isNetworkConnected(Context context){
        ConnectivityManager connectionManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectionManager != null){
            NetworkInfo info = connectionManager.getActiveNetworkInfo();
            if(info != null){
                return info.isConnected();
            }
        }
        return false;
    }
}
