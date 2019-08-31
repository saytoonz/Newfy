package com.nsromapa.frenzapp.newfy.models;

/**
 * Created by amsavarthan on 6/4/18.
 */

public class Comment extends CommentId {

    private String id,image,username, post_id, comment, timestamp;

    public Comment() {
    }

    public Comment(String id, String image, String username, String post_id, String comment, String timestamp) {
        this.id = id;
        this.image = image;
        this.username = username;
        this.post_id = post_id;
        this.comment = comment;
        this.timestamp = timestamp;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
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

    public String getPost_id() {
        return post_id;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}