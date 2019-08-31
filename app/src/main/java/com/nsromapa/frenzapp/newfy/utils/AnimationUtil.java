package com.nsromapa.frenzapp.newfy.utils;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.nsromapa.frenzapp.newfy.R;

public class AnimationUtil {

    public static void shakeView(View view, Context context){
        Animation shake= AnimationUtils.loadAnimation(context, R.anim.shake_view);
        view.startAnimation(shake);
    }

    public static void slideup(View view, Context context){
        Animation shake= AnimationUtils.loadAnimation(context, R.anim.slide_up);
        view.startAnimation(shake);
    }

    public static void slideleft(View view, Context context){
        Animation shake= AnimationUtils.loadAnimation(context, R.anim.slide_from_left);
        view.startAnimation(shake);
    }

    public static void slideright(View view, Context context){
        Animation shake= AnimationUtils.loadAnimation(context, R.anim.slide_from_right);
        view.startAnimation(shake);
    }

}
