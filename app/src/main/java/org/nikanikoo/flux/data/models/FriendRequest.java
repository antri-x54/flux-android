package org.nikanikoo.flux.data.models;

public class FriendRequest {
    private int userId;
    private String name;
    private String status;
    private String avatarUrl;
    private long timestamp;

    public FriendRequest(int userId, String name, String status, String avatarUrl, long timestamp) {
        this.userId = userId;
        this.name = name;
        this.status = status;
        this.avatarUrl = avatarUrl;
        this.timestamp = timestamp;
    }

    // Getters
    public int getUserId() { return userId; }
    public String getName() { return name; }
    public String getStatus() { return status; }
    public String getAvatarUrl() { return avatarUrl; }
    public long getTimestamp() { return timestamp; }

    // Setters
    public void setUserId(int userId) { this.userId = userId; }
    public void setName(String name) { this.name = name; }
    public void setStatus(String status) { this.status = status; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}