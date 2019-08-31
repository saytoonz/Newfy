package com.nsromapa.frenzapp.newfy.ui.activities.notification;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.nsromapa.frenzapp.newfy.R;
import com.nsromapa.frenzapp.newfy.ui.activities.friends.SendActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class NotificationActivity extends AppCompatActivity {

    private TextView nameTxt, messageTxt;
    private String msg;
    private CircleImageView imageView;

    private TextView username;
    private String user_id, current_id;
    private Button mSend;
    private EditText message;
    private FirebaseFirestore mFirestore;
    private ProgressBar mBar;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransitionExit();
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransitionEnter();
    }

    /**
     * Overrides the pending Activity transition by performing the "Enter" animation.
     */
    protected void overridePendingTransitionEnter() {
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    /**
     * Overrides the pending Activity transition by performing the "Exit" animation.
     */
    protected void overridePendingTransitionExit() {
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
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

        setContentView(R.layout.activity_notification);

        Toolbar toolbar=findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        nameTxt = (TextView) findViewById(R.id.name);
        messageTxt = (TextView) findViewById(R.id.messagetxt);
        imageView = (CircleImageView) findViewById(R.id.circleImageView);

        current_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mSend = (Button) findViewById(R.id.send);
        message = (EditText) findViewById(R.id.message);
        mBar = (ProgressBar) findViewById(R.id.progressBar);


        msg = getIntent().getStringExtra("message");
        user_id = getIntent().getStringExtra("from_id");

        mFirestore = FirebaseFirestore.getInstance();

        mFirestore.collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                String image_ = documentSnapshot.getString("image");
                CircleImageView imageView=findViewById(R.id.currentProfile);

                Glide.with(NotificationActivity.this)
                        .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                        .load(image_)
                        .into(imageView);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

        mFirestore.collection("Users").document(user_id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                String name = documentSnapshot.getString("name");
                nameTxt.setText(name);

                String image_ = documentSnapshot.getString("image");

                Glide.with(NotificationActivity.this)
                        .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                        .load(image_)
                        .into(imageView);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                imageView.setVisibility(View.GONE);
                nameTxt.setVisibility(View.GONE);
            }
        });

        messageTxt.setText(msg);


        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(getIntent().getIntExtra("notification_id", 0));
        }

        updateReadStatus();
        initReply();

    }

    private void updateReadStatus() {

        String read=getIntent().getStringExtra("read");
        if(read=="false"){
            Map<String,Object> readMap=new HashMap<>();
            readMap.put("read","true");

            mFirestore.collection("Users").document(current_id).collection("Notifications")
                    .document(getIntent().getStringExtra("doc_id")).update(readMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.i("done","read:true");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.i("error","read:false::"+e.getLocalizedMessage());
                }
            });
        }

    }


    private void initReply() {

        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String message_ = message.getText().toString();

                if (!TextUtils.isEmpty(message_)) {
                    mBar.setVisibility(View.VISIBLE);
                    Map<String, Object> notificationMessage = new HashMap<>();
                    notificationMessage.put("reply_for", msg);
                    notificationMessage.put("message", message_);
                    notificationMessage.put("from", current_id);
                    notificationMessage.put("notification_id", String.valueOf(System.currentTimeMillis()));
                    notificationMessage.put("timestamp", String.valueOf(System.currentTimeMillis()));

                    mFirestore.collection("Users/" + user_id + "/Notifications_reply").add(notificationMessage).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {

                            Toasty.success(NotificationActivity.this, "FrenzApp sent!", Toasty.LENGTH_SHORT,true).show();
                            message.setText("");
                            mBar.setVisibility(View.GONE);
                            finish();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toasty.error(NotificationActivity.this, "Error : " + e.getMessage(), Toasty.LENGTH_SHORT,true).show();
                            mBar.setVisibility(View.GONE);
                        }
                    });

                }

            }
        });

    }

    public void SendNew(View view) {

        SendActivity.startActivityExtra(NotificationActivity.this, user_id);
        overridePendingTransitionExit();

    }
}
