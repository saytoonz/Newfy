package com.nsromapa.frenzapp.newfy.models;

import java.util.List;

/**
 * Created by amsavarthan on 22/2/18.
 */

public class Friends extends UserId {

    private String name,username, image, email;
    private List<String> token_ids;

    public Friends() {

    }

    public Friends(String name, String username, String image, String email, List<String> token_ids) {
        this.name = name;
        this.username = username;
        this.image = image;
        this.email = email;
        this.token_ids = token_ids;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public List<String> getToken_ids() {
        return token_ids;
    }

    public void setToken_ids(List<String> token_ids) {
        this.token_ids = token_ids;
    }

}
