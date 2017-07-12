package com.example.xyzreader.remote;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;

public class Config {
    public static final URL BASE_URL;
    private static final String TAG = "CONFIG";

    static {
        URL url = null;
        try {
            // Original URL from Github import
            //  url = new URL("https://dl.dropboxusercontent.com/u/231329/xyzreader_data/data.json" );

            // Forum fix when original URL failed:
            // url = new URL("https://nspf.github.io/XYZReader/data.json");

            // URL taken from later version of original Github
//            url = new URL("https://go.udacity.com/xyz-reader-json" );

            // New URL suggested at first code review
            url = new URL("https://raw.githubusercontent.com/TNTest/xyzreader/master/data.json");
        } catch (MalformedURLException ignored) {
            Log.e(TAG, "Issue with internet connection - please check");
            //TODO add user-visible error message

        }

        BASE_URL = url;
    }
}
