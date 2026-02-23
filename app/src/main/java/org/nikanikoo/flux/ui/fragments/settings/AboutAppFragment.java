package org.nikanikoo.flux.ui.fragments.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.nikanikoo.flux.R;

public class AboutAppFragment extends Fragment {

    private View itemGithub;
    private View itemLicense;
    private View itemPrivacyPolicy;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about_app, container, false);
        
        initViews(view);
        setupClickListeners();
        
        return view;
    }
    
    private void initViews(View view) {
        itemGithub = view.findViewById(R.id.item_github);
    }
    
    private void setupClickListeners() {
        itemGithub.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://github.com/nikanikoo/flux-android"));
            try {
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Не удалось открыть ссылку", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
