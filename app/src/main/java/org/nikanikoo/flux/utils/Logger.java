package org.nikanikoo.flux.utils;

import android.util.Log;
import org.nikanikoo.flux.BuildConfig;

public class Logger {
    private static final boolean DEBUG_ENABLED = BuildConfig.DEBUG;
    private static final String APP_TAG = "Flux";

    public static void d(String tag, String message) {
        if (DEBUG_ENABLED) {
            Log.d(APP_TAG + ":" + tag, sanitizeMessage(message));
        }
    }
    
    public static void i(String tag, String message) {
        Log.i(APP_TAG + ":" + tag, sanitizeMessage(message));
    }
    
    public static void w(String tag, String message) {
        Log.w(APP_TAG + ":" + tag, sanitizeMessage(message));
    }
    
    public static void w(String tag, String message, Throwable throwable) {
        Log.w(APP_TAG + ":" + tag, sanitizeMessage(message), throwable);
    }
    
    public static void e(String tag, String message) {
        Log.e(APP_TAG + ":" + tag, sanitizeMessage(message));
    }
    
    public static void e(String tag, String message, Throwable throwable) {
        Log.e(APP_TAG + ":" + tag, sanitizeMessage(message), throwable);
    }

    public static void apiResponse(String tag, String response) {
        if (DEBUG_ENABLED) {
            String sanitized = sanitizeApiResponse(response);
            Log.d(APP_TAG + ":API:" + tag, sanitized);
        }
    }

    private static String sanitizeMessage(String message) {
        if (message == null) return "null";

        // Handle access_token in various formats
        message = message.replaceAll("access_token=[^&\\s]+", "access_token=***");
        message = message.replaceAll("\"access_token\"\\s*:\\s*\"[^\"]+\"", "\"access_token\":\"***\"");
        message = message.replaceAll("'access_token'\\s*:\\s*'[^']+'", "'access_token':'***'");
        message = message.replaceAll("access_token\\s*=\\s*[^&\\s,;]+", "access_token=***");

        // Handle password in various formats
        message = message.replaceAll("password=[^&\\s]+", "password=***");
        message = message.replaceAll("\"password\"\\s*:\\s*\"[^\"]+\"", "\"password\":\"***\"");
        message = message.replaceAll("'password'\\s*:\\s*'[^']+'", "'password':'***'");
        message = message.replaceAll("password\\s*=\\s*[^&\\s,;]+", "password=***");

        // Handle token in various formats
        message = message.replaceAll("token=[^&\\s]+", "token=***");
        message = message.replaceAll("\"token\"\\s*:\\s*\"[^\"]+\"", "\"token\":\"***\"");
        message = message.replaceAll("'token'\\s*:\\s*'[^']+'", "'token':'***'");
        
        return message;
    }

    private static String sanitizeApiResponse(String response) {
        if (response == null) return "null";

        if (response.length() > 1000) {
            response = response.substring(0, 1000) + "... [truncated]";
        }
        
        return sanitizeMessage(response);
    }
}