package com.rxandroidex.rxandroidexapp.app;

import android.app.Application;

public class BaseApplication extends Application {
    private static BaseApplication mInstance;
    public static BaseApplication getInstance() {
        if (mInstance == null) {
            mInstance = new BaseApplication();
        }
        return mInstance;
    }

    public BaseApplication() {
        super();
        mInstance = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
