package com.nsromapa.frenzapp.newfy.ui.activities.friends;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.nsromapa.frenzapp.newfy.R;
import com.nsromapa.frenzapp.newfy.ui.activities.SendMessage;
import com.nsromapa.frenzapp.newfy.ui.activities.notification.ImagePreview;
import com.nsromapa.frenzapp.newfy.ui.activities.notification.ImagePreviewSave;
import com.nsromapa.frenzapp.newfy.utils.AnimationUtil;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;

import es.dmoral.toasty.Toasty;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

import static com.nsromapa.frenzapp.newfy.utils.Config.random;

public class SendActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 100;
    private static final int PLACE_PICKER_REQUEST =101 ;
    TextView mSend;
    private TextView username;
    private String user_id,current_id;
    private EditText message;
    private FirebaseFirestore mFirestore;
    private Uri imageUri;
    private ImageView imagePreview;
    private String image_;
    private StorageReference storageReference;
    private GeoDataClient mGeoDataClient;
    private Place place;
    private String name;
    private ProgressDialog mDialog;
    private String c_image,c_name;
    private TextView text;
    private String f_name;

    public static void startActivityExtra(Context context,String extraString){
        Intent intent=new Intent(context,SendActivity.class);
        intent.putExtra("userId",extraString);
        context.startActivity(intent);
    }

    public static void startActivityfromAdapter(Context context,String extraString,String name){
        Intent intent=new Intent(context,SendActivity.class);
        intent.putExtra("userId",extraString);
        intent.putExtra("user__name",name);
        context.startActivity(intent);
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
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

        setContentView(R.layout.activity_send);
        final Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mDialog = new ProgressDialog(this);
        mDialog.setMessage("Please wait..");
        mDialog.setIndeterminate(true);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(false);

        imageUri=null;
        mGeoDataClient = Places.getGeoDataClient(this, null);

        mFirestore=FirebaseFirestore.getInstance();
        user_id=getIntent().getStringExtra("userId");
        current_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        storageReference= FirebaseStorage.getInstance().getReference().child("notification").child(random()+".jpg");


        imagePreview=findViewById(R.id.imagePreview);
        mSend=findViewById(R.id.send);
        message=findViewById(R.id.message);
        text=findViewById(R.id.text);

        imagePreview.setVisibility(View.GONE);
        text.setVisibility(View.VISIBLE);

        f_name=getIntent().getStringExtra("user__name");
        if(!TextUtils.isEmpty(f_name)) {
            getSupportActionBar().setTitle(f_name);
            toolbar.setTitle(f_name);
        }

        mFirestore.collection("Users").document(user_id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                f_name=documentSnapshot.getString("name");
                getSupportActionBar().setTitle(f_name);
                toolbar.setTitle(documentSnapshot.getString("name"));

                toolbar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FriendProfile.startActivity(SendActivity.this,user_id);
                    }
                });

            }
        });

        mFirestore.collection("Users").document(current_id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                c_name=documentSnapshot.getString("name");
                c_image=documentSnapshot.getString("image");
            }
        });

        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String message_=message.getText().toString();
                if(!TextUtils.isEmpty(message_)){

                    if(imageUri==null){

                        SendMessage.startActivity(SendActivity.this,"normal_message",message_,c_name,c_image,current_id,user_id);
                        //Send only message
                        message.setText("");

                    }else {

                        SendMessage.startActivity(SendActivity.this,"normal_message",message_,imageUri,c_name,c_image,current_id,user_id,f_name);
                        //Send message with Image
                        message.setText("");
                        imagePreview.setVisibility(View.GONE);
                        text.setVisibility(View.VISIBLE);

                    }


                }else{
                    AnimationUtil.shakeView(message, SendActivity.this);
                }


            }
        });

        imagePreview.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(imageUri!=null){
                    RemoveImage();
                }
                return true;
            }
        });


    }

    public void SelectImage(View view) {

        if(imageUri==null) {

            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Pick an image..."), PICK_IMAGE);

        }

    }

    private void getPhotos(String placeId) {
        final Task<PlacePhotoMetadataResponse> photoMetadataResponse = mGeoDataClient.getPlacePhotos(placeId);
        photoMetadataResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoMetadataResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlacePhotoMetadataResponse> task) {
               try {
                   PlacePhotoMetadataResponse photos = task.getResult();
                   PlacePhotoMetadataBuffer photoMetadataBuffer;

                   photoMetadataBuffer = photos.getPhotoMetadata();
                   PlacePhotoMetadata photoMetadata = photoMetadataBuffer.get(0);
                   CharSequence attribution = photoMetadata.getAttributions();
                   Task<PlacePhotoResponse> photoResponse = mGeoDataClient.getPhoto(photoMetadata);
                   photoResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoResponse>() {
                       @Override
                       public void onComplete(@NonNull Task<PlacePhotoResponse> task) {
                           PlacePhotoResponse photo = task.getResult();
                           Bitmap bitmap = photo.getBitmap();
                           imagePreview.setVisibility(View.VISIBLE);
                           text.setVisibility(View.GONE);
                           showRemoveButton();
                           imageUri = getImageUri(SendActivity.this, bitmap);
                           imagePreview.setImageURI(getImageUri(SendActivity.this, bitmap));
                       }
                   });
               }catch (Exception ex){
                   Toasty.info(SendActivity.this, "No image found for this place.", Toasty.LENGTH_SHORT,true).show();
               }
            }
        });
    }

    public Uri getImageUri(Context context, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "Title" , null);
        return Uri.parse(path);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                place = PlacePicker.getPlace(data, this);

                new MaterialDialog.Builder(this)
                        .title("Location")
                        .content("Do you want to add the place's photo?")
                        .positiveText("Yes")
                        .negativeText("No")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                getPhotos(place.getId());
                                dialog.dismiss();
                            }
                        }).onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                }).show();

               message.setText("");
                String toastMsg = String.format("Place Name: %s\nAddress: %s\nLatLng: %s,%s"
                        ,place.getName()
                        ,place.getAddress()
                        ,place.getLatLng().latitude
                        ,place.getLatLng().longitude);
                message.setText(toastMsg);
            }
        }

        if(requestCode==PICK_IMAGE){
            if(resultCode==RESULT_OK){
                new MaterialDialog.Builder(this)
                        .title("Attachment")
                        .content("Do you want to crop the image?")
                        .positiveText("Yes")
                        .negativeText("No")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                imageUri=data.getData();
                                //start crop activity
                                UCrop.Options options = new UCrop.Options();
                                options.setCompressionFormat(Bitmap.CompressFormat.PNG);
                                options.setCompressionQuality(100);
                                options.setShowCropGrid(true);

                                UCrop.of(imageUri, Uri.fromFile(new File(getCacheDir(), String.format("frenzapp_%s.png", random()))))
                                        .withOptions(options)
                                        .start(SendActivity.this);

                                dialog.dismiss();
                            }
                        }).onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        showRemoveButton();
                        imagePreview.setVisibility(View.VISIBLE);
                        text.setVisibility(View.GONE);
                        imageUri=data.getData();
                        Toasty.info(SendActivity.this, "You can long press to remove the attachment", Toasty.LENGTH_SHORT,true).show();
                        imagePreview.setImageURI(imageUri);
                        dialog.dismiss();
                    }
                }).show();
            }
        }

        if (requestCode == UCrop.REQUEST_CROP) {
            if (resultCode == RESULT_OK) {
                showRemoveButton();
                imageUri = UCrop.getOutput(data);
                imagePreview.setVisibility(View.VISIBLE);
                text.setVisibility(View.GONE);
                imagePreview.setImageURI(imageUri);

            } else if (resultCode == UCrop.RESULT_ERROR) {
                Log.e("Error", "Crop error:" + UCrop.getError(data).getMessage());
            }
        }

    }

    private void showRemoveButton() {

        findViewById(R.id.location).setAlpha(1.0f);
        findViewById(R.id.location).animate().alpha(0.0f).setDuration(500).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                findViewById(R.id.location).setVisibility(View.GONE);
                findViewById(R.id.remove).setVisibility(View.VISIBLE);
                findViewById(R.id.remove).setAlpha(0.0f);
                findViewById(R.id.remove).animate().alpha(1.0f).setDuration(500).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        findViewById(R.id.remove).setVisibility(View.VISIBLE);
                    }
                }).start();
            }
        }).start();


    }

    private void hideRemoveButton() {

        findViewById(R.id.remove).setAlpha(1.0f);
        findViewById(R.id.remove).animate().alpha(0.0f).setDuration(500).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                findViewById(R.id.remove).setVisibility(View.GONE);
                findViewById(R.id.location).setVisibility(View.VISIBLE);
                findViewById(R.id.location).setAlpha(0.0f);
                findViewById(R.id.location).animate().alpha(1.0f).setDuration(500).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        findViewById(R.id.location).setVisibility(View.VISIBLE);
                    }
                }).start();
            }
        }).start();




    }

    public void RemoveImage() {
        new MaterialDialog.Builder(this)
                .title("Attachment")
                .content("Do you want to remove the attachment?")
                .positiveText("Yes")
                .negativeText("No")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        imagePreview.setVisibility(View.GONE);
                        text.setVisibility(View.VISIBLE);
                        imageUri=null;
                        hideRemoveButton();
                        dialog.dismiss();
                    }
                }).onNegative(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                dialog.dismiss();
            }
        }).show();
    }

    public void PreviewImage(View view) {

        Intent intent=new Intent(SendActivity.this,ImagePreview.class)
                .putExtra("url","")
                .putExtra("uri",imageUri.toString());
        startActivity(intent);

    }

    public void PreviewProfileImage(View view) {

        Intent intent=new Intent(SendActivity.this,ImagePreviewSave.class)
                .putExtra("url",image_)
                .putExtra("uri","")
                .putExtra("sender_name",name);
        startActivity(intent);

    }

    public void shareLocation(View view) {

        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
            Toasty.error(this, "Some error occurred.", Toasty.LENGTH_SHORT,true).show();
            Log.e("Error",e.getMessage());
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
            Toasty.error(this, "Google play services not available", Toasty.LENGTH_SHORT,true).show();
            Log.e("Error",e.getMessage());
        }

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


    public void removeAttachment(View view) {
        RemoveImage();
    }
}
