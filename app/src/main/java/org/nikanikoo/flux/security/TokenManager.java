package org.nikanikoo.flux.security;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

/**
 * Manager for secure token storage using encrypted SharedPreferences.
 * <p>
 * SECURITY: Removed fallback to unencrypted storage. If encryption fails,
 * tokens will not be stored and user must re-authenticate.
 */
public class TokenManager {
    private static final String TAG = "TokenManager";
    private static final String PREF_NAME = "openvk_prefs";
    private static final String KEY_TOKEN = "access_token";
    private static final String KEY_INSTANCE = "instance_url";
    private static final String KEY_ENCRYPTION_FAILED = "encryption_failed";

    private final SharedPreferences prefs;
    private final boolean isEncrypted;

    /**
     * Exception thrown when encryption initialization fails
     */
    public static class EncryptionException extends Exception {
        public EncryptionException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public TokenManager(Context context) throws EncryptionException {
        SharedPreferences tempPrefs = null;
        boolean encrypted = false;
        
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            tempPrefs = EncryptedSharedPreferences.create(
                    context,
                    PREF_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
            encrypted = true;
            Log.d(TAG, "EncryptedSharedPreferences initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize encrypted storage", e);
            // SECURITY: No fallback to unencrypted storage
            throw new EncryptionException(
                "Failed to initialize encrypted token storage. " +
                "Please restart the app or reinstall if the problem persists.", e);
        }
        
        this.prefs = tempPrefs;
        this.isEncrypted = encrypted;
    }

    /**
     * Save access token securely.
     * 
     * @param token The access token to save
     * @return true if saved successfully, false otherwise
     */
    public boolean saveToken(String token) {
        if (token == null) {
            Log.w(TAG, "Attempting to save null token");
            return false;
        }
        
        if (!isEncrypted) {
            Log.e(TAG, "Cannot save token: encryption not available");
            return false;
        }
        
        try {
            prefs.edit().putString(KEY_TOKEN, token).apply();
            Log.d(TAG, "Token saved securely");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to save token", e);
            return false;
        }
    }

    /**
     * Get saved access token.
     * 
     * @return The token or null if not found
     */
    public String getToken() {
        if (!isEncrypted) {
            Log.w(TAG, "Getting token from unencrypted storage (should not happen)");
        }
        return prefs.getString(KEY_TOKEN, null);
    }

    /**
     * Save instance URL.
     * 
     * @param url The instance URL to save
     * @return true if saved successfully, false otherwise
     */
    public boolean saveInstance(String url) {
        if (url == null) {
            Log.w(TAG, "Attempting to save null instance URL");
            return false;
        }
        
        if (!isEncrypted) {
            Log.e(TAG, "Cannot save instance: encryption not available");
            return false;
        }
        
        try {
            prefs.edit().putString(KEY_INSTANCE, url).apply();
            Log.d(TAG, "Instance URL saved: " + url);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to save instance URL", e);
            return false;
        }
    }

    /**
     * Get saved instance URL.
     * 
     * @return The instance URL or default value
     */
    public String getInstance() {
        return prefs.getString(KEY_INSTANCE, "https://api.openvk.org");
    }

    /**
     * Check if encryption is enabled.
     * 
     * @return true if tokens are encrypted
     */
    public boolean isEncryptionEnabled() {
        return isEncrypted;
    }

    /**
     * Clear all saved data.
     */
    public void clear() {
        prefs.edit().clear().apply();
        Log.d(TAG, "All preferences cleared");
    }
    
    /**
     * Check if token exists.
     * 
     * @return true if token is saved
     */
    public boolean hasToken() {
        return prefs.contains(KEY_TOKEN);
    }
    
    /**
     * Delete saved token only (keep instance URL).
     */
    public void deleteToken() {
        prefs.edit().remove(KEY_TOKEN).apply();
        Log.d(TAG, "Token deleted");
    }
}
