package com.example.xyzreader.remote;

import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;

public class Config {
    public static final URL BASE_URL;
    private static final String TAG = "CONFIG";

    static {
        URL url = null;
        try {
          //  url = new URL("https://dl.dropboxusercontent.com/u/231329/xyzreader_data/data.json" );
            // Forum fix:
           // url = new URL("https://nspf.github.io/XYZReader/data.json");
            url = new URL("https://go.udacity.com/xyz-reader-json" );
        } catch (MalformedURLException ignored) {
            Log.e(TAG, "Issue with internet connection - please check");
            //TODO add user-visible error message
        }

        BASE_URL = url;
    }
}
