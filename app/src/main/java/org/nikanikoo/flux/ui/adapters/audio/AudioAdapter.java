package org.nikanikoo.flux.ui.adapters.audio;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.nikanikoo.flux.R;
import org.nikanikoo.flux.data.models.Audio;

import java.util.List;

public class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.AudioViewHolder> {

    private List<Audio> audios;
    private OnAudioClickListener listener;

    public interface OnAudioClickListener {
        void onPlayClick(Audio audio, int position);
        void onAddClick(Audio audio, int position);
        void onMoreClick(Audio audio, int position);
    }

    public AudioAdapter(List<Audio> audios, OnAudioClickListener listener) {
        this.audios = audios;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_audio, parent, false);
        return new AudioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AudioViewHolder holder, int position) {
        Audio audio = audios.get(position);
        holder.bind(audio, position);
    }

    @Override
    public int getItemCount() {
        return audios.size();
    }

    public void updateAudio(int position, Audio audio) {
        if (position >= 0 && position < audios.size()) {
            audios.set(position, audio);
            notifyItemChanged(position);
        }
    }

    class AudioViewHolder extends RecyclerView.ViewHolder {
        TextView artistText;
        TextView titleText;
        TextView durationText;
        ImageView playButton;
        ImageView addButton;
        ImageView moreButton;

        AudioViewHolder(@NonNull View itemView) {
            super(itemView);
            artistText = itemView.findViewById(R.id.audio_artist);
            titleText = itemView.findViewById(R.id.audio_title);
            durationText = itemView.findViewById(R.id.audio_duration);
            playButton = itemView.findViewById(R.id.audio_play_button);
            addButton = itemView.findViewById(R.id.audio_add_button);
            moreButton = itemView.findViewById(R.id.audio_more_button);
        }

        void bind(Audio audio, int position) {
            artistText.setText(audio.getArtist());
            titleText.setText(audio.getTitle());
            durationText.setText(audio.getFormattedDuration());

            // Обновляем иконку добавления
            if (audio.isAdded()) {
                addButton.setImageResource(R.drawable.ic_check);
                addButton.setContentDescription("Добавлено");
            } else {
                addButton.setImageResource(R.drawable.ic_add);
                addButton.setContentDescription("Добавить");
            }

            playButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPlayClick(audio, position);
                }
            });

            addButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAddClick(audio, position);
                }
            });

            moreButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMoreClick(audio, position);
                }
            });
        }
    }
}
