package org.nikanikoo.flux.ui.adapters.friends;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.squareup.picasso.Picasso;

import org.nikanikoo.flux.Constants;
import org.nikanikoo.flux.data.models.Friend;
import org.nikanikoo.flux.R;
import org.nikanikoo.flux.utils.Logger;

import java.util.List;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> {
    private static final String TAG = "FriendsAdapter";
    private List<Friend> friends;
    private Context context;
    private OnFriendClickListener listener;

    public interface OnFriendClickListener {
        void onFriendClick(Friend friend);
        void onMessageClick(Friend friend);
    }

    public FriendsAdapter(Context context, List<Friend> friends) {
        this.context = context;
        this.friends = friends;
    }

    public void setOnFriendClickListener(OnFriendClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_friend, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        Friend friend = friends.get(position);
        
        // Имя
        holder.friendName.setText(friend.getFullName());
        
        // Статус
        if (friend.getStatus() != null && !friend.getStatus().isEmpty()) {
            holder.friendStatus.setVisibility(View.VISIBLE);
            holder.friendStatus.setText(friend.getStatus());
        } else {
            holder.friendStatus.setVisibility(View.GONE);
        }
        
        // Общие друзья
        if (friend.getMutualFriends() > 0) {
            holder.mutualFriends.setVisibility(View.VISIBLE);
            String mutualText = friend.getMutualFriends() + " общих " + 
                (friend.getMutualFriends() == 1 ? "друг" : 
                 friend.getMutualFriends() < 5 ? "друга" : "друзей");
            holder.mutualFriends.setText(mutualText);
        } else {
            holder.mutualFriends.setVisibility(View.GONE);
        }
        
        // Онлайн индикатор
        holder.onlineIndicator.setVisibility(friend.isOnline() ? View.VISIBLE : View.GONE);
        
        // Галочка верификации
        Logger.d(TAG, "Friend " + friend.getFullName() + " verified: " + friend.isVerified());
        if (holder.friendVerified != null) {
            holder.friendVerified.setVisibility(friend.isVerified() ? View.VISIBLE : View.GONE);
        }
        
        // Аватар
        if (friend.getPhoto50() != null && !friend.getPhoto50().isEmpty()) {
            Picasso.get()
                    .load(friend.getPhoto50())
                    .placeholder(R.drawable.camera_200)
                    .error(R.drawable.camera_200)
                    .resize(Constants.UI.THUMBNAIL_SIZE, Constants.UI.THUMBNAIL_SIZE)
                    .centerCrop()
                    .into(holder.avatarImage);
        } else {
            holder.avatarImage.setImageResource(R.drawable.camera_200);
        }
        
        // Обработка кликов
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFriendClick(friend);
            }
        });
        
        // Делаем аватарку кликабельной
        holder.avatarImage.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFriendClick(friend);
            }
        });
        
        holder.messageButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMessageClick(friend);
            }
        });
    }

    @Override
    public int getItemCount() {
        System.out.println("FriendsAdapter: getItemCount called, returning " + friends.size());
        return friends.size();
    }

    public void updateFriends(List<Friend> newFriends) {
        System.out.println("FriendsAdapter: updateFriends called with " + newFriends.size() + " friends");
        this.friends.clear();
        this.friends.addAll(newFriends);
        System.out.println("FriendsAdapter: After update, adapter has " + this.friends.size() + " friends");
        notifyDataSetChanged();
        System.out.println("FriendsAdapter: notifyDataSetChanged called");
    }

    // Метод для освобождения ресурсов
    public void onDestroy() {
        if (friends != null) {
            friends.clear();
        }
        listener = null;
    }

    public void filterFriends(List<Friend> filteredFriends) {
        this.friends.clear();
        this.friends.addAll(filteredFriends);
        notifyDataSetChanged();
    }

    static class FriendViewHolder extends RecyclerView.ViewHolder {
        ImageView avatarImage;
        View onlineIndicator;
        TextView friendName;
        TextView friendStatus;
        TextView mutualFriends;
        MaterialButton messageButton;
        ImageView friendVerified;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarImage = itemView.findViewById(R.id.avatar_image);
            onlineIndicator = itemView.findViewById(R.id.online_indicator);
            friendName = itemView.findViewById(R.id.friend_name);
            friendStatus = itemView.findViewById(R.id.friend_status);
            mutualFriends = itemView.findViewById(R.id.mutual_friends);
            messageButton = itemView.findViewById(R.id.message_button);
            friendVerified = itemView.findViewById(R.id.friend_verified);
        }
    }
}