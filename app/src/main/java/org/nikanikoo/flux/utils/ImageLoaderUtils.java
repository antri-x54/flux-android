package org.nikanikoo.flux.utils;

import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import org.nikanikoo.flux.R;

public class ImageLoaderUtils {
    
    private static final String TAG = "ImageLoaderUtils";

    private static final int DEFAULT_AVATAR_PLACEHOLDER = R.drawable.camera_200;
    private static final int DEFAULT_IMAGE_PLACEHOLDER = R.drawable.ic_image_placeholder;
    private static final int ERROR_IMAGE = R.drawable.ic_image_error;
    
    // Image loading configuration
    private static final int AVATAR_SIZE = 200;
    private static final int PHOTO_SIZE = 800;

    private ImageLoaderUtils() {
        throw new AssertionError("Utility class");
    }

    public static void loadAvatar(String url, ImageView imageView) {
        loadImage(url, imageView, DEFAULT_AVATAR_PLACEHOLDER, DEFAULT_AVATAR_PLACEHOLDER, AVATAR_SIZE);
    }

    public static void loadAvatar(String url, ImageView imageView, int placeholderResId) {
        loadImage(url, imageView, placeholderResId, placeholderResId, AVATAR_SIZE);
    }

    public static void loadPhoto(String url, ImageView imageView) {
        loadImage(url, imageView, DEFAULT_IMAGE_PLACEHOLDER, ERROR_IMAGE, PHOTO_SIZE);
    }
    public static void loadImage(String url, ImageView imageView, int size) {
        loadImage(url, imageView, DEFAULT_IMAGE_PLACEHOLDER, ERROR_IMAGE, size);
    }
    public static void loadImage(String url, ImageView imageView, 
                                  int placeholderResId, int errorResId, int resizeSize) {
        if (imageView == null) {
            Logger.w(TAG, "ImageView is null, cannot load image");
            return;
        }
        
        if (url == null || url.isEmpty()) {
            Logger.d(TAG, "URL is empty, showing placeholder");
            imageView.setImageResource(placeholderResId);
            return;
        }
        
        try {
            RequestCreator request = Picasso.get()
                    .load(url)
                    .placeholder(placeholderResId)
                    .error(errorResId);
            
            if (resizeSize > 0) {
                request.resize(resizeSize, resizeSize)
                       .centerCrop();
            }
            
            request.into(imageView);
            
        } catch (Exception e) {
            Logger.e(TAG, "Error loading image: " + url, e);
            imageView.setImageResource(errorResId);
        }
    }

    public static void loadImageOriginal(String url, ImageView imageView,
                                          int placeholderResId, int errorResId) {
        loadImage(url, imageView, placeholderResId, errorResId, 0);
    }

    public static void clearImage(ImageView imageView) {
        if (imageView != null) {
            Picasso.get().cancelRequest(imageView);
            imageView.setImageDrawable(null);
        }
    }

    public static void preloadImage(String url) {
        if (url != null && !url.isEmpty()) {
            try {
                Picasso.get().load(url).fetch();
            } catch (Exception e) {
                Logger.w(TAG, "Error preloading image: " + url, e);
            }
        }
    }

    public static void preloadAvatar(String url) {
        preloadImage(url);
    }
}
