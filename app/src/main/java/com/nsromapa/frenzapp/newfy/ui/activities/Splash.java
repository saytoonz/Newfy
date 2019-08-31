package com.nsromapa.frenzapp.newfy.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.multidex.MultiDex;

import com.nsromapa.frenzapp.newfy.R;
import com.nsromapa.frenzapp.newfy.ui.activities.account.LoginActivity;
import com.nsromapa.frenzapp.newfy.utils.database.UserHelper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Timer;
import java.util.TimerTask;

import es.dmoral.toasty.Toasty;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class Splash extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
        MultiDex.install(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/bold.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build());

        setContentView(R.layout.activity_splash);

        /*findViewById(R.id.appname).animate()
                .alpha(1.0f)
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        findViewById(R.id.appname).setVisibility(View.VISIBLE);
                    }
                })
                .start();*/

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {

                if(FirebaseAuth.getInstance().getCurrentUser()==null){
                    startActivity(new Intent(Splash.this, LoginActivity.class));
                    finish();
                }else{

                    if(isOnline()){

                        UserHelper userHelper=new UserHelper(Splash.this);
                        Cursor rs = userHelper.getData(1);
                        rs.moveToFirst();

                         String email = rs.getString(rs.getColumnIndex(UserHelper.CONTACTS_COLUMN_EMAIL));
                        String pass = rs.getString(rs.getColumnIndex(UserHelper.CONTACTS_COLUMN_PASS));

                        if (!rs.isClosed()) {
                            rs.close();
                        }

                        FirebaseAuth.getInstance().signOut();
                        FirebaseAuth.getInstance()
                                .signInWithEmailAndPassword(email,pass)
                                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                    @Override
                                    public void onSuccess(AuthResult authResult) {

                                        MainActivity.startActivity(Splash.this,false);
                                        finish();

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        userHelper.deleteContact(1);
                                        Toasty.error(Splash.this,"Authentication revoked",Toasty.LENGTH_SHORT,true).show();
                                        startActivity(new Intent(Splash.this, LoginActivity.class));
                                        finish();

                                    }
                                });

                    }else{

                        MainActivity.startActivity(Splash.this,true);
                        finish();

                    }

                }

            }
        },1200);

    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }


}
