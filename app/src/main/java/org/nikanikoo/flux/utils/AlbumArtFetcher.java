package org.nikanikoo.flux.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// todo доделать и сделать на last.fm
public class AlbumArtFetcher {
    private static final String TAG = "AlbumArtFetcher";

    private static final String LASTFM_API_KEY = "idi v popi";
    private static final String LASTFM_BASE_URL = "";
    
    private final ExecutorService executor;
    private final Handler mainHandler;
    
    public interface AlbumArtCallback {
        void onSuccess(Bitmap bitmap);
        void onError(String error);
    }
    
    public AlbumArtFetcher() {
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public void fetchAlbumArt(String artist, String title, AlbumArtCallback callback) {
        executor.execute(() -> {
            try {
                Bitmap result = fetchFromLastFm(artist, title);

                mainHandler.post(() -> {
                    if (result != null) {
                        callback.onSuccess(result);
                    } else {
                        callback.onError("Album art not found");
                    }
                });
            } catch (Exception e) {
                Logger.e(TAG, "Error fetching album art", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    private Bitmap fetchFromLastFm(String artist, String title) {
        if (LASTFM_API_KEY.equals("idi v popi")) {
            Logger.d(TAG, "Last.fm API key not set, skipping");
            return null;
        }

        try {
            String artistEncoded = URLEncoder.encode(artist, "UTF-8");
            String titleEncoded = URLEncoder.encode(title, "UTF-8");

            String urlString = LASTFM_BASE_URL + "?method=track.getInfo" +
                    "&api_key=" + LASTFM_API_KEY +
                    "&artist=" + artistEncoded +
                    "&track=" + titleEncoded +
                    "&format=json";

            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject json = new JSONObject(response.toString());
                if (json.has("track")) {
                    JSONObject track = json.getJSONObject("track");
                    if (track.has("album")) {
                        JSONObject album = track.getJSONObject("album");
                        if (album.has("image")) {
                            JSONArray images = album.getJSONArray("image");
                            for (int i = images.length() - 1; i >= 0; i--) {
                                JSONObject image = images.getJSONObject(i);
                                String imageUrl = image.optString("#text", null);
                                if (imageUrl != null && !imageUrl.isEmpty()) {
                                    return downloadBitmap(imageUrl);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logger.e(TAG, "Error fetching from Last.fm", e);
        }
        return null;
    }

    private Bitmap downloadBitmap(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            input.close();
            return bitmap;
        } catch (Exception e) {
            Logger.e(TAG, "Error downloading bitmap", e);
            return null;
        }
    }

    public void shutdown() {
        executor.shutdown();
    }
}
