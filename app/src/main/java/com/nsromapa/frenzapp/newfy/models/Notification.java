package com.nsromapa.frenzapp.newfy.models;

public class Notification extends DocumentID{

    private String id,username,image,message,timestamp,type,action_id;

    public Notification() {
    }

    public Notification(String id, String username, String image, String message, String timestamp, String type, String action_id) {
        this.id = id;
        this.username = username;
        this.image = image;
        this.message = message;
        this.timestamp = timestamp;
        this.type = type;
        this.action_id = action_id;
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAction_id() {
        return action_id;
    }

    public void setAction_id(String action_id) {
        this.action_id = action_id;
    }
}
