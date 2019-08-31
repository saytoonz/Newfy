package com.nsromapa.frenzapp.newfy.models;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.Exclude;

public class Answers_doc_id {

    @Exclude
    public String Answers_doc_id;

    public <T extends Answers_doc_id> T withId(@NonNull final String id) {
        this.Answers_doc_id = id;
        return (T) this;
    }



}
