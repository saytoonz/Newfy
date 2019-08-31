package com.nsromapa.frenzapp.newfy.models;

public class ChatMessage extends DocumentID {

    private String message,sender_id,timestamp,image_url,sender_image;
    private boolean read;

    public ChatMessage(String message, String sender_id, String timestamp, String image_url, String sender_image, boolean read) {
        this.message = message;
        this.sender_id = sender_id;
        this.timestamp = timestamp;
        this.image_url = image_url;
        this.sender_image = sender_image;
        this.read = read;
    }

    public ChatMessage() {
    }

    public String getSender_id() {
        return sender_id;
    }

    public void setSender_id(String sender_id) {
        this.sender_id = sender_id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getSender_image() {
        return sender_image;
    }

    public void setSender_image(String sender_image) {
        this.sender_image = sender_image;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
