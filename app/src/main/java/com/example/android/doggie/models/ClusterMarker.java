package com.example.android.doggie.models;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class ClusterMarker implements ClusterItem {

    private LatLng position; // required field
    private String title; // required field
    private String snippet; // required field
    private String iconPath;
    private User user;

    public ClusterMarker(LatLng position, String title, String snippet, String iconPath, User user) {
        this.position = position;
        this.title = title;
        this.snippet = snippet;
        this.iconPath = iconPath;
        this.user = user;
    }
    public ClusterMarker(ClusterMarker marker) {
        this.position = marker.getPosition();
        this.title = marker.getTitle();
        this.snippet = marker.getSnippet();
        this.iconPath = marker.getIconPath();
        this.user = new User(marker.getUser());
    }
    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public LatLng getPosition() {
        return position;
    }

    public String getTitle() {
        return title;
    }

    public String getSnippet() {
        return snippet;
    }
}
