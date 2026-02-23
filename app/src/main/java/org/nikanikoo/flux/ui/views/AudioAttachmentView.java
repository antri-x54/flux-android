package org.nikanikoo.flux.ui.views;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.nikanikoo.flux.R;
import org.nikanikoo.flux.data.models.Audio;
import org.nikanikoo.flux.services.AudioPlayerService;

import java.util.List;

/**
 * Helper class for displaying audio attachments in posts and comments
 */
public class AudioAttachmentView {

    public interface OnAudioClickListener {
        void onAudioPlay(Audio audio);
    }

    /**
     * Add audio attachments to a container view
     * 
     * @param context Context
     * @param container Container to add audio views to
     * @param audios List of audio attachments
     * @param listener Click listener for audio playback
     */
    public static void addAudioAttachments(Context context, LinearLayout container, 
                                          List<Audio> audios, OnAudioClickListener listener) {
        if (audios == null || audios.isEmpty()) {
            return;
        }

        container.removeAllViews();
        container.setVisibility(View.VISIBLE);

        LayoutInflater inflater = LayoutInflater.from(context);

        for (int i = 0; i < audios.size(); i++) {
            Audio audio = audios.get(i);
            final int position = i;
            View audioView = inflater.inflate(R.layout.item_audio_attachment, container, false);
            
            TextView artistText = audioView.findViewById(R.id.audio_attachment_artist);
            TextView titleText = audioView.findViewById(R.id.audio_attachment_title);
            TextView durationText = audioView.findViewById(R.id.audio_attachment_duration);
            ImageView playButton = audioView.findViewById(R.id.audio_attachment_play);

            artistText.setText(audio.getArtist());
            titleText.setText(audio.getTitle());
            durationText.setText(audio.getFormattedDuration());

            playButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAudioPlay(audio);
                } else {
                    // Start player service with playlist
                    startAudioPlayer(context, audios, position);
                }
            });

            // Update play button icon based on playback state
            updatePlayButtonIcon(playButton, audio);

            container.addView(audioView);
        }
    }

    private static void updatePlayButtonIcon(ImageView playButton, Audio audio) {
        // TODO: Check if this audio is currently playing and update icon
        playButton.setImageResource(R.drawable.ic_play);
    }

    /**
     * Start audio player with playlist
     */
    private static void startAudioPlayer(Context context, List<Audio> playlist, int startPosition) {
        Intent serviceIntent = new Intent(context, AudioPlayerService.class);
        context.startService(serviceIntent);

        // Set playlist through service
        AudioPlayerHelper.setPlaylist(context, playlist, startPosition);
    }

    /**
     * Clear audio attachments from container
     * 
     * @param container Container to clear
     */
    public static void clearAudioAttachments(LinearLayout container) {
        if (container != null) {
            container.removeAllViews();
            container.setVisibility(View.GONE);
        }
    }
}

