package com.nsromapa.frenzapp.newfy.ui.activities.account;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.transition.Fade;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.nsromapa.frenzapp.newfy.R;
import com.nsromapa.frenzapp.newfy.ui.activities.MainActivity;
import com.nsromapa.frenzapp.newfy.utils.AnimationUtil;
import com.nsromapa.frenzapp.newfy.utils.Config;
import com.nsromapa.frenzapp.newfy.utils.database.UserHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.marcoscg.dialogsheet.DialogSheet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();
    public static Activity activity;
    private EditText email,password;
    private Button login,register;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private UserHelper userHelper;
    private ProgressDialog mDialog;

    public static void startActivityy(Context context) {
        Intent intent=new Intent(context,LoginActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    private void askPermission() {

        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                )
                .withListener(new MultiplePermissionsListener() {

                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            Toasty.info(LoginActivity.this, "You have denied some permissions permanently, if the app force close try granting permission from settings.", Toasty.LENGTH_LONG, true).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .check();

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

        setContentView(R.layout.activity_login);

        activity = this;
        mAuth=FirebaseAuth.getInstance();
        mFirestore=FirebaseFirestore.getInstance();
        userHelper = new UserHelper(this);

        askPermission();
        mDialog = new ProgressDialog(this);
        mDialog.setMessage("Please wait..");
        mDialog.setIndeterminate(true);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(false);

        email=findViewById(R.id.email);
        password=findViewById(R.id.password);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Fade fade = new Fade();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                fade.excludeTarget(findViewById(R.id.layout), true);
                fade.excludeTarget(android.R.id.statusBarBackground, true);
                fade.excludeTarget(android.R.id.navigationBarBackground, true);
                getWindow().setEnterTransition(fade);
                getWindow().setExitTransition(fade);
            }
        }

    }


    public void performLogin() {

        final String email_, pass_;
        email_ = email.getText().toString();
        pass_ = password.getText().toString();

        if (!TextUtils.isEmpty(email_) && !TextUtils.isEmpty(pass_)) {
            mDialog.show();

            mAuth.signInWithEmailAndPassword(email_, pass_).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull final Task<AuthResult> task) {
                    if (task.isSuccessful()) {

                        Log.i(TAG, "Login Successful, continue to email verified");

                        if (task.getResult().getUser().isEmailVerified()) {

                            Log.i(TAG, "Email is verified Successful, continue to get token");
                            FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                @Override
                                public void onComplete(@NonNull Task<InstanceIdResult> taskInstanceToken) {
                                    if (!taskInstanceToken.isSuccessful()) {
                                        Log.w(TAG, "getInstanceId failed", taskInstanceToken.getException());
                                        return;
                                    }

                                    // TODO Get new Instance ID token
                                    final String token_id = taskInstanceToken.getResult().getToken();

                                    Log.i(TAG, "Get Token Listener, Token ID (token_id): " + token_id);

                                    final String current_id = task.getResult().getUser().getUid();


                                    mFirestore.collection("Users").document(current_id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                // TODO How to update only one field that is list of string.
                                                //https://firebase.google.com/docs/firestore/manage-data/add-data#update-data

                                                final Map<String, Object> tokenMap = new HashMap<>();
                                                tokenMap.put("token_ids", FieldValue.arrayUnion(token_id));

                                                mFirestore.collection("Users")
                                                        .document(current_id)
                                                        .update(tokenMap)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {

                                                                FirebaseFirestore.getInstance().collection("Users").document(current_id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                                    @Override
                                                                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                                                                        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, MODE_PRIVATE);
                                                                        SharedPreferences.Editor editor = pref.edit();
                                                                        editor.putString("regId", token_id);
                                                                        editor.apply();

                                                                        String username = documentSnapshot.getString("username");
                                                                        String name = documentSnapshot.getString("name");
                                                                        String email = documentSnapshot.getString("email");
                                                                        String image = documentSnapshot.getString("image");
                                                                        String password = pass_;
                                                                        String location = documentSnapshot.getString("location");
                                                                        String bio = documentSnapshot.getString("bio");

                                                                        userHelper.insertContact(username, name, email, image, password, location, bio);

                                                                        mDialog.dismiss();
                                                                        MainActivity.startActivity(LoginActivity.this);
                                                                        finish();

                                                                    }
                                                                }).addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Log.e("Error", ".." + e.getMessage());
                                                                        mDialog.dismiss();
                                                                    }
                                                                });

                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        mDialog.dismiss();
                                                        Toasty.error(LoginActivity.this, "Error: " + e.getMessage(), Toasty.LENGTH_SHORT,true).show();
                                                    }
                                                });

                                        }


                                    });

                                }

                            });


                        }

                        else{

                            mDialog.dismiss();
                            new DialogSheet(LoginActivity.this)
                                    .setTitle("Information")
                                    .setCancelable(true)
                                    .setRoundedCorners(true)
                                    .setColoredNavigationBar(true)
                                    .setMessage("Email has not been verified, please verify and continue.")
                                    .setPositiveButton("Send again", v -> task.getResult()
                                            .getUser()
                                            .sendEmailVerification()
                                            .addOnSuccessListener(aVoid -> Toasty.success(LoginActivity.this, "Verification email sent", Toasty.LENGTH_SHORT,true).show())
                                            .addOnFailureListener(e -> Log.e("Error",e.getMessage())))
                                    .setNegativeButton("Ok", v -> {

                                    })
                                    .show();

                            if (mAuth.getCurrentUser() != null) {
                                mAuth.signOut();
                            }

                        }

                    } else {
                        if (task.getException().getMessage().contains("The password is invalid")) {
                            Toasty.error(LoginActivity.this, "Error: The password you have entered is invalid.", Toasty.LENGTH_SHORT,true).show();
                            mDialog.dismiss();
                        } else if (task.getException().getMessage().contains("There is no user record")) {
                            Toasty.error(LoginActivity.this, "Error: Invalid user, Please register using the button below.", Toasty.LENGTH_SHORT,true).show();
                            mDialog.dismiss();
                        } else {
                            Toasty.error(LoginActivity.this, "Error: " + task.getException().getMessage(), Toasty.LENGTH_SHORT,true).show();
                            mDialog.dismiss();
                        }

                    }
                }
            });

        } else if (TextUtils.isEmpty(email_)) {

            AnimationUtil.shakeView(email, this);

        } else if (TextUtils.isEmpty(pass_)) {

            AnimationUtil.shakeView(password, this);

        } else {

            AnimationUtil.shakeView(email, this);
            AnimationUtil.shakeView(password, this);

        }

    }


    public void onLogin(View view) {
        performLogin();
    }

    public void onRegister(View view) {
        RegisterActivity.startActivity(this);
    }

    public void onForgotPassword(View view) {

        if(TextUtils.isEmpty(email.getText().toString())) {
            Toasty.info(activity, "Enter your email to send reset password mail.", Toasty.LENGTH_SHORT,true).show();
            AnimationUtil.shakeView(email, this);
        }else{

            mDialog.show();

            FirebaseAuth.getInstance().sendPasswordResetEmail(email.getText().toString())
                    .addOnSuccessListener(aVoid -> {
                        mDialog.dismiss();
                        Toasty.success(LoginActivity.this, "Reset password mail sent", Toasty.LENGTH_SHORT,true).show();
                    })
                    .addOnFailureListener(e -> {
                        mDialog.dismiss();
                        Toasty.error(LoginActivity.this, "Error sending mail : "+e.getLocalizedMessage(), Toasty.LENGTH_SHORT,true).show();
                    });
        }

    }
}
