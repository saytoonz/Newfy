package com.nsromapa.frenzapp.newfy.ui.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.nsromapa.frenzapp.newfy.R;
import com.nsromapa.frenzapp.newfy.service.MessageService;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;

import static com.nsromapa.frenzapp.newfy.utils.Config.random;

public class SendMessage extends AppCompatActivity {

    private LottieAnimationView lottieAnimationView;
    SharedPreferences sharedPreferences;
    private int serviceCount;

    public static void startActivity(Context context, String reason, String message_, Uri imageUri, String c_name, String c_image, String current_id, String user_id, String f_name){
        context.startActivity(new Intent(context, SendMessage.class)
                .putExtra("message_",message_)
                .putExtra("imageUri",imageUri.toString())
                .putExtra("c_name",c_name)
                .putExtra("reason",reason)
                .putExtra("c_image",c_image)
                .putExtra("current_id",current_id)
                .putExtra("f_name",f_name)
                .putExtra("user_id",user_id));
    }
    public static void startActivity(Context context,String reason,String message_,String c_name,String c_image,String current_id,String user_id){
        context.startActivity(new Intent(context, SendMessage.class)
                .putExtra("message_",message_)
                .putExtra("reason",reason)
                .putExtra("c_name",c_name)
                .putExtra("c_image",c_image)
                .putExtra("current_id",current_id)
                .putExtra("user_id",user_id));
    }

    public static void startActivity(Context context,String reason,String dev_id){
        context.startActivity(new Intent(context, SendMessage.class)
                .putExtra("reason",reason)
        .putExtra("dev_id",dev_id));
    }

    FirebaseFirestore mFirestore;
    StorageReference storageReference;

    @Override
    public void onBackPressed() {
        //do nothing
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);
        mFirestore=FirebaseFirestore.getInstance();
        storageReference= FirebaseStorage.getInstance().getReference().child("notification").child("IMG_"+System.currentTimeMillis()+"_"+random()+".jpg");

        sharedPreferences=getSharedPreferences("messageservice",MODE_PRIVATE);
        serviceCount=sharedPreferences.getInt("count",0);

        lottieAnimationView=findViewById(R.id.lottie);
        lottieAnimationView.useHardwareAcceleration(true);

        String reason=getIntent().getStringExtra("reason");

        if(reason.equals("normal_message")) {
            if (StringUtils.isNotEmpty(getIntent().getStringExtra("imageUri"))) {
                lottieAnimationView.playAnimation();
                lottieAnimationView.addAnimatorListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);

                        sharedPreferences.edit().putInt("count", ++serviceCount).apply();

                        Intent intent=new Intent(SendMessage.this, MessageService.class);
                        intent.putExtra("count",serviceCount);
                        intent.putExtra("f_name",getIntent().getStringExtra("f_name"));
                        intent.putExtra("message",getIntent().getStringExtra("message_"));
                        intent.putExtra("imageUri",getIntent().getStringExtra("imageUri"));
                        intent.putExtra("c_name",getIntent().getStringExtra("c_name"));
                        intent.putExtra("c_image",getIntent().getStringExtra("c_image"));
                        intent.putExtra("current_id",getIntent().getStringExtra("current_id"));
                        intent.putExtra("user_id",getIntent().getStringExtra("user_id"));
                        intent.setAction(MessageService.ACTION_START_FOREGROUND_SERVICE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            startForegroundService(intent);
                        }else {
                            startService(intent);
                        }
                        Toasty.info(SendMessage.this,"Message will be sent after the image uploads",Toasty.LENGTH_SHORT,true).show();
                        finish();
                    }
                });

            } else {
                sendMessage(
                        "",
                        getIntent().getStringExtra("message_"),
                        null,
                        getIntent().getStringExtra("c_name"),
                        getIntent().getStringExtra("c_image"),
                        getIntent().getStringExtra("current_id"),
                        getIntent().getStringExtra("user_id"));
            }
        }else if(reason.equals("wish_back")){
            FirebaseFirestore.getInstance().collection("Users")
                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> sendMessage(
                            "festival",
                            documentSnapshot.getString("name") + " wished you back for your wishes."
                            ,null
                            ,documentSnapshot.getString("username")
                            ,documentSnapshot.getString("image")
                            ,documentSnapshot.getString("id")
                            ,getIntent().getStringExtra("dev_id")));
        }
        else if(reason.equals("thank_back")){
            FirebaseFirestore.getInstance().collection("Users")
                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            sendMessage(
                                    "festival",
                                    documentSnapshot.getString("name") + " thanked you back for your wishes."
                                    ,null
                                    ,documentSnapshot.getString("username")
                                    ,documentSnapshot.getString("image")
                                    ,documentSnapshot.getString("id")
                                    ,getIntent().getStringExtra("dev_id"));
                        }
                    });
        }

    }

    private void sendMessage(final String type, final String message_, Uri imageUri, final String c_name, final String c_image, final String current_id, final String user_id) {

        if(!TextUtils.isEmpty(message_)){

            if(imageUri==null){

                //Send only message

                Map<String,Object> notificationMessage=new HashMap<>();
                notificationMessage.put("username",c_name);
                notificationMessage.put("userimage",c_image);
                notificationMessage.put("message",message_);
                notificationMessage.put("from",current_id);
                notificationMessage.put("notification_id", String.valueOf(System.currentTimeMillis()));
                notificationMessage.put("timestamp", String.valueOf(System.currentTimeMillis()));
                notificationMessage.put("read","false");

                mFirestore.collection("Users/"+user_id+"/Notifications").add(notificationMessage).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        lottieAnimationView.playAnimation();
                        lottieAnimationView.addAnimatorListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                Toasty.success(SendMessage.this, "Message sent", Toasty.LENGTH_SHORT,true).show();
                                finish();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toasty.error(SendMessage.this, "Error sending message :(", Toasty.LENGTH_LONG,true).show();

                    }
                });
            }


        }else{
            Toasty.warning(this, "Message cannot be empty", Toasty.LENGTH_SHORT,true).show();
            finish();
        }

    }

}
