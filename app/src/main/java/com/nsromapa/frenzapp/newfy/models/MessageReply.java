package com.nsromapa.frenzapp.newfy.models;

public class MessageReply extends MessageId {

    private String from,read, message, notification_id, reply_for, reply_image, timestamp;

    public MessageReply() {
    }

    public MessageReply(String from, String message, String notification_id, String reply_for, String reply_image, String timestamp, String read) {
        this.from = from;
        this.message = message;
        this.notification_id = notification_id;
        this.reply_for = reply_for;
        this.reply_image = reply_image;
        this.timestamp = timestamp;
        this.read = read;
    }

    public String getReply_image() {
        return reply_image;
    }

    public void setReply_image(String reply_image) {
        this.reply_image = reply_image;
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

    public String getNotification_id() {
        return notification_id;
    }

    public void setNotification_id(String notification_id) {
        this.notification_id = notification_id;
    }

    public String getReply_for() {
        return reply_for;
    }

    public void setReply_for(String reply_for) {
        this.reply_for = reply_for;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getRead() {
        return read;
    }

    public void setRead(String read) {
        this.read = read;
    }
}
