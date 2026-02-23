package org.nikanikoo.flux.utils;

import android.content.Context;

import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;

public class SSLHelper {
    
    private static final String TAG = "SSLHelper";

    private static final Set<String> TRUSTED_PINS = new HashSet<>();
    
    static {}

    private SSLHelper() {
        throw new AssertionError("Utility class");
    }

    public static OkHttpClient.Builder configureSecureSSL(OkHttpClient.Builder builder) {
        try {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm()
            );
            tmf.init((KeyStore) null);

            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, tmf.getTrustManagers(), null);

            X509TrustManager trustManager = null;
            for (TrustManager tm : tmf.getTrustManagers()) {
                if (tm instanceof X509TrustManager) {
                    trustManager = (X509TrustManager) tm;
                    break;
                }
            }
            
            if (trustManager == null) {
                throw new IllegalStateException("No X509TrustManager found");
            }
            
            builder.sslSocketFactory(sslContext.getSocketFactory(), trustManager);

            builder.hostnameVerifier(HttpsURLConnection.getDefaultHostnameVerifier());
            
            Logger.d(TAG, "SSL configured with secure settings");
            return builder;
            
        } catch (Exception e) {
            Logger.e(TAG, "Error configuring SSL", e);
            throw new RuntimeException("Failed to configure SSL", e);
        }
    }

    @Deprecated
    public static OkHttpClient.Builder configureWithUserTrust(
            OkHttpClient.Builder builder, 
            Context context, 
            String hostname) {
        try {
            final SSLCertificateManager certManager = SSLCertificateManager.getInstance(context);

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm()
            );
            tmf.init((KeyStore) null);
            
            final X509TrustManager defaultTrustManager = findX509TrustManager(tmf);

            X509TrustManager customTrustManager = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) 
                        throws CertificateException {
                    defaultTrustManager.checkClientTrusted(chain, authType);
                }
                
                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) 
                        throws CertificateException {
                    try {
                        defaultTrustManager.checkServerTrusted(chain, authType);
                    } catch (CertificateException e) {
                        if (chain.length > 0) {
                            X509Certificate cert = chain[0];
                            if (certManager.isCertificateTrusted(hostname, cert)) {
                                Logger.d(TAG, "Using user-trusted certificate for: " + hostname);
                                return;
                            }
                        }
                        throw e;
                    }
                }
                
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return defaultTrustManager.getAcceptedIssuers();
                }
            };

            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, new TrustManager[]{customTrustManager}, null);
            
            builder.sslSocketFactory(sslContext.getSocketFactory(), customTrustManager);

            builder.hostnameVerifier(certManager.createHostnameVerifier(context, hostname));
            
            Logger.d(TAG, "SSL configured with user trust for: " + hostname);
            return builder;
            
        } catch (Exception e) {
            Logger.e(TAG, "Error configuring SSL with user trust", e);
            throw new RuntimeException("Failed to configure SSL", e);
        }
    }

    private static X509TrustManager findX509TrustManager(TrustManagerFactory tmf) {
        for (TrustManager tm : tmf.getTrustManagers()) {
            if (tm instanceof X509TrustManager) {
                return (X509TrustManager) tm;
            }
        }
        throw new IllegalStateException("No X509TrustManager found");
    }

    public static boolean matchesPin(X509Certificate cert) {
        try {
            String fingerprint = SSLCertificateManager.getCertificateFingerprint(cert);
            String pin = "sha256/" + fingerprint.replace(":", "");
            return TRUSTED_PINS.contains(pin);
        } catch (Exception e) {
            Logger.e(TAG, "Error checking certificate pin", e);
            return false;
        }
    }

    public static SSLSocketFactory getDefaultSSLSocketFactory() throws Exception {
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(null, null, null);
        return sslContext.getSocketFactory();
    }

    public static HostnameVerifier getDefaultHostnameVerifier() {
        return HttpsURLConnection.getDefaultHostnameVerifier();
    }

    public static void addTrustedPin(String pin) {
        TRUSTED_PINS.add(pin);
    }

    public static void clearTrustedPins() {
        TRUSTED_PINS.clear();
    }

    @Deprecated
    public static OkHttpClient.Builder configureToIgnoreSSL(OkHttpClient.Builder builder) {
        try {
            final X509TrustManager trustAllManager = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) 
                        throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) 
                        throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            };

            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{trustAllManager}, 
                new java.security.SecureRandom());

            final HostnameVerifier allHostsValid = new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

            builder.sslSocketFactory(sslContext.getSocketFactory(), trustAllManager);
            builder.hostnameVerifier(allHostsValid);

            builder.connectionSpecs(java.util.Arrays.asList(
                ConnectionSpec.MODERN_TLS, 
                ConnectionSpec.COMPATIBLE_TLS,
                ConnectionSpec.CLEARTEXT
            ));

            builder.followSslRedirects(true);
            builder.followRedirects(true);

            Logger.w(TAG, "SSL certificate validation is DISABLED! " +
                "This is insecure and should only be used for testing or " +
                "when connecting to instances with self-signed certificates.");
            
            return builder;
        } catch (Exception e) {
            Logger.e(TAG, "Error configuring SSL to ignore certificates", e);
            throw new RuntimeException("Failed to configure SSL", e);
        }
    }
}
