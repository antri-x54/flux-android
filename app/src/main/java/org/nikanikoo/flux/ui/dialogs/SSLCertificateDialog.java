package org.nikanikoo.flux.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import org.nikanikoo.flux.R;

/**
 * Dialog for SSL certificate error confirmation
 */
public class SSLCertificateDialog extends DialogFragment {
    
    public interface SSLDialogCallback {
        void onAccept();
        void onReject();
    }
    
    private static final String ARG_HOSTNAME = "hostname";
    private static final String ARG_FINGERPRINT = "fingerprint";
    private static final String ARG_SUBJECT = "subject";
    private static final String ARG_ISSUER = "issuer";
    
    private SSLDialogCallback callback;
    
    public static SSLCertificateDialog newInstance(String hostname, String fingerprint, 
                                                    String subject, String issuer) {
        SSLCertificateDialog dialog = new SSLCertificateDialog();
        Bundle args = new Bundle();
        args.putString(ARG_HOSTNAME, hostname);
        args.putString(ARG_FINGERPRINT, fingerprint);
        args.putString(ARG_SUBJECT, subject);
        args.putString(ARG_ISSUER, issuer);
        dialog.setArguments(args);
        return dialog;
    }
    
    public void setCallback(SSLDialogCallback callback) {
        this.callback = callback;
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String hostname = getArguments().getString(ARG_HOSTNAME, "Unknown");
        String fingerprint = getArguments().getString(ARG_FINGERPRINT, "Unknown");
        String subject = getArguments().getString(ARG_SUBJECT, "Unknown");
        String issuer = getArguments().getString(ARG_ISSUER, "Unknown");
        
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        
        // Inflate custom layout
        android.view.View view = getLayoutInflater().inflate(R.layout.dialog_ssl_certificate, null);
        
        TextView tvHostname = view.findViewById(R.id.ssl_hostname);
        TextView tvSubject = view.findViewById(R.id.ssl_subject);
        TextView tvIssuer = view.findViewById(R.id.ssl_issuer);
        TextView tvFingerprint = view.findViewById(R.id.ssl_fingerprint);
        Button btnAccept = view.findViewById(R.id.ssl_accept);
        Button btnReject = view.findViewById(R.id.ssl_reject);
        
        tvHostname.setText(hostname);
        tvSubject.setText(formatDN(subject));
        tvIssuer.setText(formatDN(issuer));
        tvFingerprint.setText(fingerprint);
        
        btnAccept.setOnClickListener(v -> {
            if (callback != null) callback.onAccept();
            dismiss();
        });
        
        btnReject.setOnClickListener(v -> {
            if (callback != null) callback.onReject();
            dismiss();
        });
        
        builder.setView(view);
        builder.setCancelable(false);
        
        return builder.create();
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
}
