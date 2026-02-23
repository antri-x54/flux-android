package org.nikanikoo.flux.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;

public class SSLCertificateManager {
    private static final String TAG = "SSLCertificateManager";
    private static final String PREFS_NAME = "ssl_cert_prefs";
    private static final String KEY_TRUSTED_CERTS = "trusted_certificates";
    
    private final SharedPreferences prefs;
    private static SSLCertificateManager instance;
    
    public interface CertificateCallback {
        void onTrusted();
        void onRejected();
    }
    
    private SSLCertificateManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    public static synchronized SSLCertificateManager getInstance(Context context) {
        if (instance == null) {
            instance = new SSLCertificateManager(context.getApplicationContext());
        }
        return instance;
    }

    public static String getCertificateFingerprint(X509Certificate cert) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] fingerprint = md.digest(cert.getEncoded());
            StringBuilder sb = new StringBuilder();
            for (byte b : fingerprint) {
                sb.append(String.format("%02X:", b));
            }
            return sb.substring(0, sb.length() - 1);
        } catch (Exception e) {
            Logger.e(TAG, "Error getting fingerprint", e);
            return "Unknown";
        }
    }

    public boolean isCertificateTrusted(String hostname, X509Certificate cert) {
        String certKey = getCertKey(hostname, cert);
        Set<String> trustedCerts = prefs.getStringSet(KEY_TRUSTED_CERTS, new HashSet<>());
        return trustedCerts.contains(certKey);
    }

    public HostnameVerifier createHostnameVerifier(Context context, String expectedHostname) {
        return (hostname, session) -> {
            try {
                X509Certificate[] certs = (X509Certificate[]) session.getPeerCertificates();
                if (certs.length > 0) {
                    X509Certificate cert = certs[0];

                    if (isCertificateTrusted(hostname, cert)) {
                        return true;
                    }

                    if (hostname.equals(expectedHostname) || hostname.endsWith("." + expectedHostname)) {
                        return true;
                    }
                }
            } catch (Exception e) {
                Logger.e(TAG, "Error verifying hostname", e);
            }
            return false;
        };
    }
    

    private String formatDN(String dn) {
        String[] parts = dn.split(",");
        for (String part : parts) {
            part = part.trim();
            if (part.startsWith("CN=")) {
                return part.substring(3);
            }
        }
        return dn;
    }

    private String getCertKey(String hostname, X509Certificate cert) {
        try {
            return hostname + "|" + getCertificateFingerprint(cert);
        } catch (Exception e) {
            return hostname;
        }
    }
}
