package com.nsromapa.frenzapp.newfy.ui.activities.account;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
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
import com.nsromapa.frenzapp.newfy.utils.AnimationUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;


public class RegisterActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 100;
    public Uri imageUri;
    public StorageReference storageReference;
    public ProgressDialog mDialog;
    public String name_, pass_, email_, username_, location_;
    private EditText name, email, password, location, username;
    private CircleImageView profile_image;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;

    public static void startActivity(Context context) {
        context.startActivity(new Intent(context, RegisterActivity.class));
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
                            Toasty.info(RegisterActivity.this, "You have denied some permissions permanently, if the app force close try granting permission from settings.", Toasty.LENGTH_LONG, true).show();
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

        setContentView(R.layout.activity_register);

        askPermission();

        mAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference().child("images");
        firebaseFirestore = FirebaseFirestore.getInstance();
        imageUri = null;

        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        location = findViewById(R.id.location);
        username = findViewById(R.id.username);

        /*LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        Location location1 = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, true));
        Geocoder geocoder=new Geocoder(getBaseContext(), Locale.getDefault());
        List<Address> addresses;

        try {
            addresses=geocoder.getFromLocation(location1.getLatitude(),location1.getLongitude(),1);
            if(addresses.size()>0){
                location.setText(addresses.get(0).getLocality());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        mDialog = new ProgressDialog(this);
        mDialog.setMessage("Please wait..");
        mDialog.setIndeterminate(true);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(false);

        Button register = findViewById(R.id.button);

        profile_image=findViewById(R.id.profile_image);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Fade fade = new Fade();
            fade.excludeTarget(findViewById(R.id.layout), true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                fade.excludeTarget(android.R.id.statusBarBackground, true);
                fade.excludeTarget(android.R.id.navigationBarBackground, true);
                getWindow().setEnterTransition(fade);
                getWindow().setExitTransition(fade);
            }
        }

        register.setOnClickListener(view -> {

            if(imageUri!=null){
                username_=username.getText().toString();
                name_=name.getText().toString();
                email_=email.getText().toString();
                pass_=password.getText().toString();
                location_=location.getText().toString();

                mDialog.show();

                if (TextUtils.isEmpty(username_) ) {
                    AnimationUtil.shakeView(username, RegisterActivity.this);
                    mDialog.dismiss();

                }else if(username_.length()<5){

                    Toasty.error(getApplicationContext(),"Username should be more than 5 characters",Toasty.LENGTH_SHORT,true).show();
                    AnimationUtil.shakeView(username, RegisterActivity.this);
                    mDialog.dismiss();

                }else if(!username_.matches("[a-zA-Z._]*")){

                    Toasty.error(getApplicationContext(),"No numbers or special character than period and underscore allowed",Toasty.LENGTH_SHORT,true).show();
                    AnimationUtil.shakeView(username, RegisterActivity.this);
                    mDialog.dismiss();

                }

                if (TextUtils.isEmpty(name_) && !name_.matches("[a-zA-Z ]*")) {

                    Toasty.error(getApplicationContext(),"Invalid name",Toasty.LENGTH_SHORT,true).show();
                    AnimationUtil.shakeView(name, RegisterActivity.this);
                    mDialog.dismiss();

                }
                if (TextUtils.isEmpty(email_)) {

                    AnimationUtil.shakeView(email, RegisterActivity.this);
                    mDialog.dismiss();

                }
                if (TextUtils.isEmpty(pass_)) {

                    AnimationUtil.shakeView(password, RegisterActivity.this);
                    mDialog.dismiss();

                }

                if (TextUtils.isEmpty(location_)) {

                    AnimationUtil.shakeView(location, RegisterActivity.this);
                    mDialog.dismiss();

                }

                if (!TextUtils.isEmpty(name_) || !TextUtils.isEmpty(email_) ||
                        !TextUtils.isEmpty(pass_) || !TextUtils.isEmpty(username_) || !TextUtils.isEmpty(location_)) {

                    firebaseFirestore.collection("Usernames")
                            .document(username_)
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if(!documentSnapshot.exists()){
                                        registerUser();
                                    }else{
                                        Toasty.error(RegisterActivity.this, "Username already exists", Toasty.LENGTH_SHORT,true).show();
                                        AnimationUtil.shakeView(username, RegisterActivity.this);
                                        mDialog.dismiss();
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e("Error",e.getMessage());
                                }
                            });

                }else{

                    AnimationUtil.shakeView(username, RegisterActivity.this);
                    AnimationUtil.shakeView(name, RegisterActivity.this);
                    AnimationUtil.shakeView(email, RegisterActivity.this);
                    AnimationUtil.shakeView(password, RegisterActivity.this);
                    AnimationUtil.shakeView(location, RegisterActivity.this);
                    mDialog.dismiss();

                }

            }else{
                AnimationUtil.shakeView(profile_image, RegisterActivity.this);
                Toasty.warning(RegisterActivity.this, "We recommend you to set a profile picture", Toasty.LENGTH_SHORT,true).show();
                mDialog.dismiss();
            }

        });


    }

    private void registerUser() {

        mAuth.createUserWithEmailAndPassword(email_, pass_).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull final Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    Map<String,Object> usernameMap=new HashMap<String, Object>();
                    usernameMap.put("username",username_);

                    firebaseFirestore.collection("Usernames")
                            .document(username_)
                            .set(usernameMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    task.getResult()
                                            .getUser()
                                            .sendEmailVerification()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    final String userUid = task.getResult().getUser().getUid();
                                                    final StorageReference user_profile = storageReference.child(userUid + ".png");
                                                    user_profile.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
                                                            if (task.isSuccessful()) {

                                                                user_profile.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                                    @Override
                                                                    public void onSuccess(final Uri uri) {

                                                                        // TODO https://firebase.google.com/docs/cloud-messaging/android/client#retrieve-the-current-registration-token.

                                                                        Map<String, Object> userMap = new HashMap<>();
                                                                        userMap.put("id", userUid);
                                                                        userMap.put("name", name_);
                                                                        userMap.put("image", uri.toString());
                                                                        userMap.put("email", email_);
                                                                        userMap.put("bio",getString(R.string.default_bio));
                                                                        userMap.put("username", username_);
                                                                        userMap.put("location", location_);

                                                                        firebaseFirestore.collection("Users").document(userUid).set(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {

                                                                                mDialog.dismiss();
                                                                                Toasty.success(RegisterActivity.this, "Verification email sent", Toasty.LENGTH_SHORT,true).show();
                                                                                finish();

                                                                            }
                                                                        }).addOnFailureListener(new OnFailureListener() {
                                                                            @Override
                                                                            public void onFailure(@NonNull Exception e) {
                                                                                mDialog.dismiss();
                                                                                Toasty.error(RegisterActivity.this, "Error: " + e.getMessage(), Toasty.LENGTH_SHORT,true).show();
                                                                            }
                                                                        });


                                                                    }
                                                                }).addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        mDialog.dismiss();
                                                                    }
                                                                });


                                                            } else {
                                                                mDialog.dismiss();
                                                            }
                                                        }
                                                    });

                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    task.getResult().getUser().delete();
                                                }
                                            });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e("Error",e.getMessage());
                                }
                            });


                } else {
                    mDialog.dismiss();
                    Toasty.error(RegisterActivity.this, "Error: " + task.getException().getMessage(), Toasty.LENGTH_SHORT,true).show();
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==PICK_IMAGE){
            if(resultCode==RESULT_OK){
                imageUri=data.getData();
                // start crop activity
                UCrop.Options options = new UCrop.Options();
                options.setCompressionFormat(Bitmap.CompressFormat.PNG);
                options.setCompressionQuality(100);
                options.setShowCropGrid(true);

                UCrop.of(imageUri, Uri.fromFile(new File(getCacheDir(), "frenzapp_user_profile_picture.png")))
                        .withAspectRatio(1, 1)
                        .withOptions(options)
                        .start(this);

            }
        }
        if (requestCode == UCrop.REQUEST_CROP) {
            if (resultCode == RESULT_OK) {
                imageUri = UCrop.getOutput(data);
                profile_image.setImageURI(imageUri);

            } else if (resultCode == UCrop.RESULT_ERROR) {
                Log.e("Error", "Crop error:" + UCrop.getError(data).getMessage());
            }
        }


    }

    public void setProfilepic(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), PICK_IMAGE);
    }

    public void onLogin(View view) {
        onBackPressed();
    }

    public void openPolicy(View view) {
        String url = "http://lvamsavarthan.github.io/lvstore/hify_privacy_policy.html";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    public void openTerms(View view) {
        String url = "http://lvamsavarthan.github.io/lvstore/hify_terms.html";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }
}
