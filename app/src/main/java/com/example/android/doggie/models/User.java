package com.example.android.doggie.models;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable{

    private String email;
    private String userId;
    private String username;
    private String imagePath;
    private String isRunning;
    private String isLogOut;
    private String online;

    public User(String email, String userId, String username, String imagePath) {
        this.email = email;
        this.userId = userId;
        this.username = username;
        this.imagePath = imagePath;
        this.isRunning = "Yes";
        this.isLogOut = "No";
        this.online = "Yes"; // primitive boolean is not supported in the Parcel Object
    }
    public User(User user) {
        this.email = user.getEmail();
        this.userId = user.getUserId();
        this.username = user.getUsername();
        this.imagePath = user.getImagePath();
        this.isRunning = "Yes";
        this.isLogOut = "No";
        this.online = "Yes"; // primitive boolean is not supported in the Parcel Object
    }
    public User() {
        isRunning = "Yes";
        isLogOut = "No";
        updateOnlineStatus();
    }
    protected User(Parcel in) {
        email = in.readString();
        userId = in.readString();
        username = in.readString();
        imagePath = in.readString();
        isRunning = in.readString();
        isLogOut = in.readString();
        online = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public static Creator<User> getCREATOR() {
        return CREATOR;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void updateOnlineStatus()
    {
        if (isLogOut.equals("No") && isRunning.equals("Yes"))
            this.online = "Yes";
        else
            this.online = "No";
    }

    public void setOnline(String online)
    {
        this.online = online;
    }
    public String getOnline()
    {
        updateOnlineStatus();
        return online;
    }

    public void setLogOut(String isLogOut)
    {
        this.isLogOut = isLogOut;
    }
    public String getIsLogOut()
    {
        return isLogOut;
    }
    public void setRunning(String isRunning)
    {
        this.isRunning = isRunning;
    }
    public String getIsRunning()
    {
        return isRunning;
    }
    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", imagePath='" + imagePath + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(email);
        dest.writeString(userId);
        dest.writeString(username);
        dest.writeString(imagePath);
        dest.writeString(isRunning);
        dest.writeString(isLogOut);
        dest.writeString(online);
    }
}
