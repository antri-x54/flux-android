package org.nikanikoo.flux.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AsyncTaskHelper {
    private static final String TAG = "AsyncTaskHelper";
    private static final int THREAD_POOL_SIZE = 4;
    
    private static final ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE, r -> {
        Thread t = new Thread(r);
        t.setName("AsyncTask-" + t.getId());
        t.setPriority(Thread.NORM_PRIORITY - 1);
        return t;
    });
    
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public static <T> Future<?> executeAsync(Callable<T> task, AsyncCallback<T> callback) {
        return executor.submit(() -> {
            try {
                T result = task.call();
                mainHandler.post(() -> callback.onSuccess(result));
            } catch (Exception e) {
                Logger.e(TAG, "Error in async task", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    public static Future<?> executeAsync(Runnable task, SimpleCallback callback) {
        return executor.submit(() -> {
            try {
                task.run();
                mainHandler.post(callback::onComplete);
            } catch (Exception e) {
                Logger.e(TAG, "Error in async task", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    public static <T> Future<?> parseAsync(Callable<T> parser, AsyncCallback<T> callback) {
        return executeAsync(parser, callback);
    }

    public static void runOnUiThread(Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            mainHandler.post(runnable);
        }
    }

    public static boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    public static void shutdown() {
        executor.shutdown();
    }

    public interface AsyncCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    public interface SimpleCallback {
        void onComplete();
        void onError(String error);
    }
}
