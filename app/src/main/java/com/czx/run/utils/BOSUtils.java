package com.czx.run.utils;

import com.baidubce.auth.DefaultBceCredentials;
import com.baidubce.services.bos.BosClient;
import com.baidubce.services.bos.BosClientConfiguration;

/**
 * Created by czx on 2016/7/3.
 */
public class BOSUtils {

    private static final String ACCESS_KEY = "b43f8e4214e6497dbf6aee6315aa9b69";
    private static final String SECRET_KEY = "2c02b93def274d7e827f008ee2c05155";
    private static final String ENDPOINT = "http://gz.bcebos.com";
    public static final String BUCKET ="codeczx";

    private static volatile BosClient mBosClient;

    private BOSUtils(){}

    public static BosClient getInstance(){
        if(mBosClient == null){
            synchronized(BOSUtils.class){
                if(mBosClient == null){
                    mBosClient = getClient();
                }
            }
        }
        return mBosClient;
    }

    private static BosClient getClient() {
        BosClientConfiguration config = new BosClientConfiguration();
        config.setCredentials(new DefaultBceCredentials(ACCESS_KEY,SECRET_KEY));
        config.setEndpoint(ENDPOINT);
        config.setConnectionTimeoutInMillis(5000);
        return new BosClient(config);
    }
}
