package com.nsromapa.frenzapp.newfy.models;
import androidx.annotation.NonNull;

/**
 * Created by amsavarthan on 22/2/18.
 */

public class AcceptedValue {

    public boolean accepted;

    public <T extends AcceptedValue> T withAccepted(@NonNull final boolean id) {
        this.accepted = id;
        return (T) this;
    }

}
