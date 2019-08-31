package com.nsromapa.frenzapp.newfy.models;

import android.content.Intent;

/**
 * Created by amsavarthan on 15/3/18.
 */

public class Extras {

    public String title, subtitle;
    public int image;
    public Intent intent;

    public Extras() {
    }

    public Extras(String title, String subtitle, int image, Intent intent) {
        this.title = title;
        this.subtitle = subtitle;
        this.image = image;
        this.intent = intent;
    }

    public Intent getIntent() {

        return intent;
    }

    public void setIntent(Intent intent) {
        this.intent = intent;
    }


    public String getTitle() {

        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }
}
