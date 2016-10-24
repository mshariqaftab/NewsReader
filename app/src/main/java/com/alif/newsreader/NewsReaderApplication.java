package com.alif.newsreader;

import android.app.Application;
import android.util.Log;

import com.alif.newsreader.util.NewsReaderCrashLibrary;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ConnectionQuality;
import com.androidnetworking.interfaces.ConnectionQualityChangeListener;

import timber.log.Timber;


public class NewsReaderApplication extends Application {
    private static NewsReaderApplication appInstance = null;

    @Override
    public void onCreate() {
        super.onCreate();
        appInstance = this;

        // Added timber configuration on application level
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new CrashReportingTree());
        }

        AndroidNetworking.initialize(getApplicationContext());
        AndroidNetworking.enableLogging();
        AndroidNetworking.setConnectionQualityChangeListener(new ConnectionQualityChangeListener() {
            @Override
            public void onChange(ConnectionQuality currentConnectionQuality, int currentBandwidth) {
                Timber.d("onChange: currentConnectionQuality : " + currentConnectionQuality + " currentBandwidth : " + currentBandwidth);
            }
        });
    }

    /**
     * A tree which logs important information for crash reporting.
     */
    private static class CrashReportingTree extends Timber.Tree {
        @Override
        protected void log(int priority, String tag, String message, Throwable t) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG) {
                return;
            }

            NewsReaderCrashLibrary.log(priority, tag, message);

            if (t != null) {
                if (priority == Log.ERROR) {
                    NewsReaderCrashLibrary.logError(t);
                } else if (priority == Log.WARN) {
                    NewsReaderCrashLibrary.logWarning(t);
                }
            }
        }
    }
}
