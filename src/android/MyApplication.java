package com.chinamobile.gdwy;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;

/**
 * Created by liangzhongtai on 2020/3/19.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // 友盟统计
        UMConfigure.init(getApplicationContext(), UMConfigure.DEVICE_TYPE_PHONE,"");
        // MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO);
        MobclickAgent.setCatchUncaughtExceptions(true);
        UMConfigure.setLogEnabled(true);


        this.registerActivityLifecycleCallbacks(lifecycle);
        // this.unregisterActivityLifecycleCallbacks(lifecycle);
    }

    private Application.ActivityLifecycleCallbacks lifecycle = new Application.ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        }

        @Override
        public void onActivityStarted(Activity activity) {
        }

        @Override
        public void onActivityResumed(Activity activity) {
            MobclickAgent.onResume(activity);
        }

        @Override
        public void onActivityPaused(Activity activity) {
        }

        @Override
        public void onActivityStopped(Activity activity) {
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            MobclickAgent.onPause(activity);
        }
    };
}
