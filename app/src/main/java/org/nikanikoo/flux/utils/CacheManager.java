package org.nikanikoo.flux.utils;

import android.content.Context;

import org.nikanikoo.flux.Constants;

import java.io.File;
import java.io.IOException;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class CacheManager {
    private static final String TAG = "CacheManager";
    private static final int CACHE_SIZE_BYTES = (int) (Constants.Cache.HTTP_CACHE_SIZE_MB * 1024 * 1024);

    private final Cache cache;

    public CacheManager(Context context) {
        File cacheDir = new File(context.getCacheDir(), "http_cache");
        cache = new Cache(cacheDir, CACHE_SIZE_BYTES);
    }

    public Cache getCache() {
        return cache;
    }

    public static Interceptor getCacheInterceptor() {
        return chain -> {
            Request request = chain.request();
            Response response = chain.proceed(request);

            String cacheControl = request.header("Cache-Control");
            if (cacheControl == null || !cacheControl.contains("no-cache")) {
                response = response.newBuilder()
                        .header("Cache-Control", "public, max-age=300")
                        .build();
            }

            return response;
        };
    }

    public static Interceptor getForceCacheInterceptor() {
        return chain -> {
            Request request = chain.request();
            String cacheControl = request.header("Cache-Control");

            if (cacheControl == null) {
                request = request.newBuilder()
                        .cacheControl(new okhttp3.CacheControl.Builder()
                                .maxStale(7, java.util.concurrent.TimeUnit.DAYS)
                                .build())
                        .build();
            }

            Response response = chain.proceed(request);

            if (!isNetworkAvailable()) {
                response = response.newBuilder()
                        .header("Cache-Control", "public, only-if-cached")
                        .build();
            }

            return response;
        };
    }

    private static boolean isNetworkAvailable() {
        return true;
    }

    public void evictAll() {
        try {
            cache.evictAll();
            Logger.d(TAG, "Cache evicted");
        } catch (IOException e) {
            Logger.e(TAG, "Error evicting cache", e);
        }
    }

    public long getCacheSize() {
        try {
            return cache.size();
        } catch (IOException e) {
            Logger.e(TAG, "Error getting cache size", e);
            return 0;
        }
    }

    public long getMaxCacheSize() {
        return cache.maxSize();
    }
}
