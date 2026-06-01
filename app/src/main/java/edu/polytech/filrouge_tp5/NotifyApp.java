package edu.polytech.filrouge_tp5;

import android.app.Application;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Date;

public class NotifyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        installCrashLogger();
        Notifier.ensureChannel(this);
        MapConfig.configure(this);
    }

    private void installCrashLogger() {
        final Thread.UncaughtExceptionHandler def = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Log.e("SIGNALROUTE_CRASH", "Crash non gere", throwable);
            try {
                File file = new File(getExternalFilesDir(null), "crash.txt");
                PrintWriter writer = new PrintWriter(new FileWriter(file, false));
                writer.println(new Date().toString());
                throwable.printStackTrace(writer);
                writer.flush();
                writer.close();
            } catch (Exception ignored) {
            }
            if (def != null) {
                def.uncaughtException(thread, throwable);
            }
        });
    }
}
