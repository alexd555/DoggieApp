package com.example.android.doggie;

import android.app.Application;

import com.example.android.doggie.models.User;


public class UserClient extends Application {

    private User user = null;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
