package com.nsromapa.frenzapp.newfy.models;

/**
 * Created by amsavarthan on 22/2/18.
 */

public class Message extends MessageId {

    private String from,read, message,userimage,username,timestamp,notification_id,image;
    public Message() {
    }


    public Message(String from, String message, String userimage, String username, String timestamp, String notification_id, String image, String read) {
        this.from = from;
        this.message = message;
        this.userimage = userimage;
        this.username = username;
        this.timestamp = timestamp;
        this.notification_id = notification_id;
        this.image = image;
        this.read = read;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUserimage() {
        return userimage;
    }

    public void setUserimage(String userimage) {
        this.userimage = userimage;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getNotification_id() {
        return notification_id;
    }

    public void setNotification_id(String notification_id) {
        this.notification_id = notification_id;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRead() {
        return read;
    }

    public void setRead(String read) {
        this.read = read;
    }
}

