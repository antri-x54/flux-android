package org.nikanikoo.flux.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.squareup.picasso.Picasso;

import org.nikanikoo.flux.R;
import org.nikanikoo.flux.data.models.Video;
import org.nikanikoo.flux.ui.activities.VideoPlayerActivity;

public class VideoAttachmentView extends FrameLayout {

    private ImageView thumbnail;
    private TextView title;
    private TextView duration;
    private ImageView playIcon;
    private Video video;
    private Context context;

    public VideoAttachmentView(@NonNull Context context) {
        super(context);
        this.context = context;
        init(context);
    }

    public VideoAttachmentView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init(context);
    }

    public VideoAttachmentView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_video_attachment, this, true);
        
        thumbnail = findViewById(R.id.video_thumbnail);
        title = findViewById(R.id.video_title);
        duration = findViewById(R.id.video_duration);
        playIcon = findViewById(R.id.video_play_icon);

        setOnClickListener(v -> {
            if (video != null) {
                VideoPlayerActivity.start(context, video);
            }
        });
    }

    public void setVideo(Video video) {
        this.video = video;
        
        if (video == null) {
            setVisibility(GONE);
            return;
        }

        setVisibility(VISIBLE);
        
        title.setText(video.getTitle());
        duration.setText(video.getFormattedDuration());

        // Load thumbnail
        String imageUrl = video.getImage();
        if (imageUrl == null || imageUrl.isEmpty()) {
            imageUrl = video.getFirstFrame();
        }
        
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_video_placeholder)
                    .error(R.drawable.ic_video_placeholder)
                    .into(thumbnail);
        } else {
            thumbnail.setImageResource(R.drawable.ic_video_placeholder);
        }
    }
}
