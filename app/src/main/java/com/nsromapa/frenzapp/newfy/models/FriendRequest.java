package com.nsromapa.frenzapp.newfy.models;

import java.util.List;

/**
 * Created by amsavarthan on 11/3/18.
 */

public class FriendRequest extends UserId {

    private String id,username, name, email, image,timestamp;
    // token field no longer used
    private List<String> token_ids;

    public FriendRequest() {
    }

    public FriendRequest(String id, String username, String name, String email, String image, List<String> token_ids, String timestamp) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.email = email;
        this.image = image;
        this.token_ids = token_ids;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public List<String> getToken_ids() {
        return token_ids;
    }

    public void setToken_ids(List<String> token_ids) {
        this.token_ids = token_ids;
    }
}