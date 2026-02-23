package org.nikanikoo.flux.ui.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.nikanikoo.flux.R;
import org.nikanikoo.flux.data.models.Audio;
import org.nikanikoo.flux.services.AudioPlayerService;
import org.nikanikoo.flux.utils.Logger;

/**
 * Controller для управления Mini Player в MainActivity.
 * Инкапсулирует логику работы с AudioPlayerService и обновления UI плеера.
 */
public class MiniPlayerController {
    
    private static final String TAG = "MiniPlayerController";
    
    private final MainActivity activity;
    
    // Views
    private LinearLayout miniPlayerContainer;
    private TextView miniPlayerTitle;
    private TextView miniPlayerArtist;
    private ImageButton miniPlayerPlayPause;
    
    // Service
    private AudioPlayerService playerService;
    private boolean playerServiceBound = false;
    
    // Callback для уведомления Activity об изменениях
    private OnPlayerStateChangeListener stateChangeListener;
    
    public interface OnPlayerStateChangeListener {
        void onPlayerConnected();
        void onPlayerDisconnected();
        void onTrackChanged(Audio audio);
    }
    
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AudioPlayerService.AudioBinder binder = (AudioPlayerService.AudioBinder) service;
            playerService = binder.getService();
            playerServiceBound = true;
            Logger.d(TAG, "AudioPlayerService connected");
            
            updateUI();
            
            if (stateChangeListener != null) {
                stateChangeListener.onPlayerConnected();
            }
        }
        
        @Override
        public void onServiceDisconnected(ComponentName name) {
            playerServiceBound = false;
            playerService = null;
            Logger.d(TAG, "AudioPlayerService disconnected");
            
            if (stateChangeListener != null) {
                stateChangeListener.onPlayerDisconnected();
            }
        }
    };
    
    public MiniPlayerController(MainActivity activity) {
        this.activity = activity;
    }
    
    /**
     * Инициализация View
     */
    public void initViews(View rootView) {
        miniPlayerContainer = rootView.findViewById(R.id.mini_player_container);
        miniPlayerTitle = rootView.findViewById(R.id.mini_player_title);
        miniPlayerArtist = rootView.findViewById(R.id.mini_player_artist);
        miniPlayerPlayPause = rootView.findViewById(R.id.mini_player_play_pause);
        
        setupClickListeners();
    }
    
    /**
     * Настройка обработчиков кликов
     */
    private void setupClickListeners() {
        if (miniPlayerPlayPause != null) {
            miniPlayerPlayPause.setOnClickListener(v -> togglePlayPause());
        }
        
        if (miniPlayerContainer != null) {
            miniPlayerContainer.setOnClickListener(v -> openFullPlayer());
        }
    }
    
    /**
     * Привязка к AudioPlayerService
     */
    public void bindService() {
        Intent intent = new Intent(activity, AudioPlayerService.class);
        activity.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }
    
    /**
     * Отвязка от AudioPlayerService
     */
    public void unbindService() {
        if (playerServiceBound) {
            activity.unbindService(serviceConnection);
            playerServiceBound = false;
        }
    }
    
    /**
     * Play/Pause
     */
    private void togglePlayPause() {
        if (!playerServiceBound || playerService == null) {
            return;
        }
        
        if (playerService.isPlaying()) {
            playerService.pause();
        } else {
            playerService.play();
        }
        
        updatePlayPauseButton();
    }
    
    /**
     * Открыть полноэкранный плеер
     */
    private void openFullPlayer() {
        if (!playerServiceBound || playerService == null) {
            return;
        }
        
        Intent intent = new Intent(activity, AudioPlayerActivity.class);
        activity.startActivity(intent);
    }
    
    /**
     * Обновить UI плеера
     */
    public void updateUI() {
        if (!playerServiceBound || playerService == null) {
            hidePlayer();
            return;
        }
        
        Audio currentTrack = playerService.getCurrentAudio();
        if (currentTrack == null) {
            hidePlayer();
            return;
        }
        
        showPlayer();
        
        if (miniPlayerTitle != null) {
            miniPlayerTitle.setText(currentTrack.getTitle());
        }
        
        if (miniPlayerArtist != null) {
            miniPlayerArtist.setText(currentTrack.getArtist());
        }
        
        updatePlayPauseButton();
        
        if (stateChangeListener != null) {
            stateChangeListener.onTrackChanged(currentTrack);
        }
    }
    
    /**
     * Обновить кнопку Play/Pause
     */
    private void updatePlayPauseButton() {
        if (miniPlayerPlayPause == null || !playerServiceBound) {
            return;
        }
        
        if (playerService.isPlaying()) {
            miniPlayerPlayPause.setImageResource(R.drawable.ic_pause);
        } else {
            miniPlayerPlayPause.setImageResource(R.drawable.ic_play);
        }
    }
    
    /**
     * Показать плеер
     */
    private void showPlayer() {
        if (miniPlayerContainer != null) {
            miniPlayerContainer.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * Скрыть плеер
     */
    private void hidePlayer() {
        if (miniPlayerContainer != null) {
            miniPlayerContainer.setVisibility(View.GONE);
        }
    }
    
    /**
     * Проверить, привязан ли сервис
     */
    public boolean isBound() {
        return playerServiceBound;
    }
    
    /**
     * Получить сервис плеера
     */
    public AudioPlayerService getPlayerService() {
        return playerService;
    }
    
    /**
     * Установить слушатель изменений состояния
     */
    public void setOnPlayerStateChangeListener(OnPlayerStateChangeListener listener) {
        this.stateChangeListener = listener;
    }
    
    /**
     * Получить текущий трек
     */
    public Audio getCurrentTrack() {
        if (playerServiceBound && playerService != null) {
            return playerService.getCurrentAudio();
        }
        return null;
    }
    
    /**
     * Проверить, воспроизводится ли музыка
     */
    public boolean isPlaying() {
        if (playerServiceBound && playerService != null) {
            return playerService.isPlaying();
        }
        return false;
    }
}
