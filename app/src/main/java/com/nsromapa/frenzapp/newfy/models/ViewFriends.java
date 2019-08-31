package com.nsromapa.frenzapp.newfy.models;

/**
 * Created by amsavarthan on 22/2/18.
 */

public class ViewFriends {

    private String id,username, name, image, email, token_id;

    public ViewFriends() {
    }

    public ViewFriends(String id, String username, String name, String image, String email, String token_id) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.image = image;
        this.email = email;
        this.token_id = token_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken_id() {
        return token_id;
    }

    public void setToken_id(String token_id) {
        this.token_id = token_id;
    }

}
