package com.oguzhandongul.floatinglogcat;

import android.app.Application;
import android.content.Context;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

public class ApplicationClass extends Application {

    private static ApplicationClass instance;
    private static Bus mEventBus;


    public ApplicationClass() {
        instance = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mEventBus = new Bus(ThreadEnforcer.ANY);
        instance = this;


    }


    public static Bus getEventBus() {
        if (mEventBus == null) {
            mEventBus = new Bus(ThreadEnforcer.ANY);
        }
        return mEventBus;
    }


    public static Context getContext() {
        if (instance == null) {
            instance = new ApplicationClass();
        }
        return instance;
    }


}
