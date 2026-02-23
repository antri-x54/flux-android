package org.nikanikoo.flux.data.models;

public class Message {
    private int id;
    private int userId;
    private int fromId;
    private String text;
    private long date;
    private boolean out;
    private boolean readState;
    private String userName;
    private String userPhoto;
    private boolean userVerified;

    public Message(int id, int userId, int fromId, String text, long date, boolean out, boolean readState) {
        this.id = id;
        this.userId = userId;
        this.fromId = fromId;
        this.text = text;
        this.date = date;
        this.out = out;
        this.readState = readState;
    }

    // Getters
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public int getFromId() { return fromId; }
    public String getText() { return text; }
    public long getDate() { return date; }
    public boolean isOut() { return out; }
    public boolean isReadState() { return readState; }
    public String getUserName() { return userName; }
    public String getUserPhoto() { return userPhoto; }
    public boolean isUserVerified() { return userVerified; }

    // Setters
    public void setUserName(String userName) { this.userName = userName; }
    public void setUserPhoto(String userPhoto) { this.userPhoto = userPhoto; }
    public void setReadState(boolean readState) { this.readState = readState; }
    public void setUserVerified(boolean userVerified) { this.userVerified = userVerified; }
}