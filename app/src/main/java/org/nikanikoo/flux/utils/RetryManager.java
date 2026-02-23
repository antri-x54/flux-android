package org.nikanikoo.flux.utils;

import android.os.Handler;
import android.os.Looper;

import org.nikanikoo.flux.Constants;

import java.util.Locale;

public class RetryManager {
    
    private static final double DEFAULT_BACKOFF_MULTIPLIER = 2.0;
    
    private final Handler handler;
    
    public RetryManager() {
        this.handler = new Handler(Looper.getMainLooper());
    }
    

    public interface RetryableOperation {
        void execute(RetryCallback callback);
    }

    public interface RetryCallback {
        void onSuccess();
        void onFailure(String error, boolean shouldRetry);
    }

    public static class RetryConfig {
        public final int maxRetries;
        public final long initialDelay;
        public final double backoffMultiplier;
        
        public RetryConfig(int maxRetries, long initialDelay, double backoffMultiplier) {
            this.maxRetries = maxRetries;
            this.initialDelay = initialDelay;
            this.backoffMultiplier = backoffMultiplier;
        }
        
        public static RetryConfig defaultConfig() {
            return new RetryConfig(Constants.Api.MAX_RETRY_ATTEMPTS, Constants.Api.RETRY_DELAY_MS, DEFAULT_BACKOFF_MULTIPLIER);
        }
        
        public static RetryConfig networkConfig() {
            return new RetryConfig(5, 2000, 1.5);
        }
        
        public static RetryConfig quickConfig() {
            return new RetryConfig(2, 500, 1.2);
        }
    }

    public void executeWithRetry(RetryableOperation operation, RetryConfig config, RetryCallback finalCallback) {
        executeWithRetryInternal(operation, config, finalCallback, 0, config.initialDelay);
    }

    public void executeWithRetry(RetryableOperation operation, RetryCallback finalCallback) {
        executeWithRetry(operation, RetryConfig.defaultConfig(), finalCallback);
    }
    
    private void executeWithRetryInternal(RetryableOperation operation, RetryConfig config, 
                                        RetryCallback finalCallback, int currentAttempt, long currentDelay) {
        
        operation.execute(new RetryCallback() {
            @Override
            public void onSuccess() {
                Logger.d("RetryManager", "Operation succeeded on attempt " + (currentAttempt + 1));
                finalCallback.onSuccess();
            }
            
            @Override
            public void onFailure(String error, boolean shouldRetry) {
                Logger.w("RetryManager", "Operation failed on attempt " + (currentAttempt + 1) + ": " + error);
                
                if (!shouldRetry || currentAttempt >= config.maxRetries - 1) {
                    Logger.e("RetryManager", "Operation failed after " + (currentAttempt + 1) + " attempts");
                    finalCallback.onFailure(error, false);
                    return;
                }
                

                long nextDelay = (long) (currentDelay * config.backoffMultiplier);
                Logger.d("RetryManager", "Scheduling retry " + (currentAttempt + 2) + " in " + nextDelay + "ms");
                
                handler.postDelayed(() -> {
                    executeWithRetryInternal(operation, config, finalCallback, currentAttempt + 1, nextDelay);
                }, currentDelay);
            }
        });
    }

    public static boolean shouldRetryForError(String error) {
        if (error == null) return false;
        
        String lowerError = error.toLowerCase(Locale.ROOT);

        if (lowerError.contains("timeout") || 
            lowerError.contains("connection") ||
            lowerError.contains("network") ||
            lowerError.contains("socket") ||
            lowerError.contains("unreachable")) {
            return true;
        }

        if (lowerError.contains("500") || 
            lowerError.contains("502") ||
            lowerError.contains("503") ||
            lowerError.contains("504")) {
            return true;
        }

        if (lowerError.contains("401") || 
            lowerError.contains("403") ||
            lowerError.contains("invalid token") ||
            lowerError.contains("access denied")) {
            return false;
        }

        if (lowerError.contains("400") || 
            lowerError.contains("invalid") ||
            lowerError.contains("bad request")) {
            return false;
        }

        return false;
    }

    public void cancelAll() {
        handler.removeCallbacksAndMessages(null);
        Logger.d("RetryManager", "All retry operations cancelled");
    }
}