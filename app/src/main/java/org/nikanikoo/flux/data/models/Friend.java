package org.nikanikoo.flux.data.models;

import org.json.JSONObject;

public class Friend {
    private int id;
    private String firstName;
    private String lastName;
    private String photo50;
    private String photo100;
    private boolean online;
    private String screenName;
    private String status;
    private int mutualFriends;
    private boolean verified;

    public Friend() {}

    public static Friend fromJson(JSONObject json) {
        Friend friend = new Friend();
        try {
            friend.id = json.optInt("id", 0);
            friend.firstName = json.optString("first_name", "");
            friend.lastName = json.optString("last_name", "");
            friend.photo50 = json.optString("photo_50", "");
            friend.photo100 = json.optString("photo_100", "");
            friend.online = json.optInt("online", 0) == 1;
            friend.screenName = json.optString("screen_name", "");
            friend.status = json.optString("status", "");
            // verified может быть int 1/0 или boolean
            if (json.has("verified")) {
                Object verifiedObj = json.opt("verified");
                if (verifiedObj instanceof Integer) {
                    friend.verified = (Integer) verifiedObj == 1;
                } else if (verifiedObj instanceof Boolean) {
                    friend.verified = (Boolean) verifiedObj;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return friend;
    }

    // Getters
    public int getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getFullName() { return firstName + " " + lastName; }
    public String getPhoto50() { return photo50; }
    public String getPhoto100() { return photo100; }
    public boolean isOnline() { return online; }
    public String getScreenName() { return screenName; }
    public String getStatus() { return status; }
    public int getMutualFriends() { return mutualFriends; }
    public boolean isVerified() { return verified; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setPhoto50(String photo50) { this.photo50 = photo50; }
    public void setPhoto100(String photo100) { this.photo100 = photo100; }
    public void setOnline(boolean online) { this.online = online; }
    public void setScreenName(String screenName) { this.screenName = screenName; }
    public void setStatus(String status) { this.status = status; }
    public void setMutualFriends(int mutualFriends) { this.mutualFriends = mutualFriends; }
    public void setVerified(boolean verified) { this.verified = verified; }
}