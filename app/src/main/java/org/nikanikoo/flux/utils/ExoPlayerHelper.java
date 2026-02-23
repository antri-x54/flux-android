package org.nikanikoo.flux.utils;

import android.content.Context;

import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.datasource.okhttp.OkHttpDataSource;

import org.nikanikoo.flux.Constants;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class ExoPlayerHelper {

    @OptIn(markerClass = UnstableApi.class)
    public static DataSource.Factory createDataSourceFactory(Context context) {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .connectTimeout(Constants.Api.CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(Constants.Api.READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(Constants.Api.WRITE_TIMEOUT, TimeUnit.SECONDS)
                .followRedirects(true)
                .followSslRedirects(true);

        SSLHelper.configureToIgnoreSSL(clientBuilder);
        
        OkHttpClient client = clientBuilder.build();

        OkHttpDataSource.Factory httpDataSourceFactory = new OkHttpDataSource.Factory(client)
                .setUserAgent("Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.120 Mobile Safari/537.36");

        return new DefaultDataSource.Factory(context, httpDataSourceFactory);
    }
}
