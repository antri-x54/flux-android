package org.nikanikoo.flux.ui.adapters.posts;

import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import org.nikanikoo.flux.Constants;
import java.util.List;

public class PostImagesAdapter extends RecyclerView.Adapter<PostImagesAdapter.ImageViewHolder> {
    private List<String> imageUrls;

    public PostImagesAdapter(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageView imageView = new ImageView(parent.getContext());
        
        // Настройка параметров ImageView
        int imageSize = (int) (Constants.UI.THUMBNAIL_SIZE * parent.getContext().getResources().getDisplayMetrics().density);
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(imageSize, imageSize);
        layoutParams.setMarginEnd((int) (8 * parent.getContext().getResources().getDisplayMetrics().density));
        imageView.setLayoutParams(layoutParams);
        
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setBackground(parent.getContext().getDrawable(android.R.drawable.gallery_thumb));
        
        return new ImageViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);
        
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Picasso.get()
                    .load(imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .resize(400, 400)
                    .centerCrop()
                    .into(holder.imageView);
        } else {
            holder.imageView.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }

    @Override
    public int getItemCount() {
        return imageUrls != null ? imageUrls.size() : 0;
    }

    public void updateImages(List<String> newImageUrls) {
        this.imageUrls = newImageUrls;
        notifyDataSetChanged();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        ImageViewHolder(@NonNull ImageView itemView) {
            super(itemView);
            this.imageView = itemView;
        }
    }
}