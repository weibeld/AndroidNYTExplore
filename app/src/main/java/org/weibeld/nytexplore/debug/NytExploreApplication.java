package org.weibeld.nytexplore.debug;

import android.app.Application;

import com.facebook.stetho.Stetho;

/**
 * This subclass of Application is solely used to initialise Stetho (Chrome-based debugging tool).
 */
public class NytExploreApplication extends Application {
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
    }
}
