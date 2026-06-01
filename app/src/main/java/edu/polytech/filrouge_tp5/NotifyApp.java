package edu.polytech.filrouge_tp5;

import android.app.Application;

public class NotifyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Notifier.ensureChannel(this);
        MapConfig.configure(this);
    }
}
