package org.nikanikoo.flux.ui.fragments.media;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;

import org.nikanikoo.flux.R;
import org.nikanikoo.flux.utils.Logger;

public class PhotoFragment extends Fragment {
    
    private static final String TAG = "PhotoFragment";
    private static final String ARG_IMAGE_URL = "image_url";
    
    private String imageUrl;
    private PhotoView photoView;
    
    // Интерфейс для обработки тапов
    public interface OnPhotoTapListener {
        void onPhotoTap();
    }
    
    public static PhotoFragment newInstance(String imageUrl) {
        PhotoFragment fragment = new PhotoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_IMAGE_URL, imageUrl);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            imageUrl = getArguments().getString(ARG_IMAGE_URL);
        }
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_photo, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        photoView = view.findViewById(R.id.photoView);
        
        // Настраиваем обработчик тапов
        photoView.setOnPhotoTapListener((view1, x, y) -> {
            // Ищем родительский PhotoViewerFragment и вызываем toggleUI
            Fragment parentFragment = getParentFragment();
            if (parentFragment instanceof PhotoViewerFragment) {
                ((PhotoViewerFragment) parentFragment).toggleUI();
            }
            Logger.d(TAG, "Photo tapped at coordinates: " + x + ", " + y);
        });
        
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Logger.d(TAG, "Loading image: " + imageUrl);
            Picasso.get()
                    .load(imageUrl)
                    .fit()
                    .centerInside()
                    .into(photoView);
        }
    }
}