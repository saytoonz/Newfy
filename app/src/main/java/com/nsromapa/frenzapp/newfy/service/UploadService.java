package com.nsromapa.frenzapp.newfy.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.nsromapa.frenzapp.newfy.models.Images;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import id.zelory.compressor.Compressor;

public class UploadService extends Service {

    public static final String ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE" ;
    private static final String TAG_FOREGROUND_SERVICE = UploadService.class.getSimpleName();
    public static final String ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE";
    private int count;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent!=null){

            String action = intent.getAction();
            if(action.equals(ACTION_START_FOREGROUND_SERVICE)){

                List<Images>imagesList=intent.getParcelableArrayListExtra("imagesList");
                int notification_id=intent.getIntExtra("notification_id",2);
                String current_id=intent.getStringExtra("current_id");
                String description=intent.getStringExtra("description");
                ArrayList<String> uploadedImagesUrl=intent.getStringArrayListExtra("uploadedImagesUrl");
                count=intent.getIntExtra("count",0);
                uploadImages(notification_id,0,imagesList,current_id,description,uploadedImagesUrl);

            }

        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void stopForegroundService(boolean removeNotification)
    {
        Log.d(TAG_FOREGROUND_SERVICE, "Stop foreground service.");
        // Stop foreground service and remove the notification.
        stopForeground(removeNotification);
        // Stop the foreground service.
        stopSelf();
    }

    private void notifyProgress(int id,int icon,String title,String message, Context context,int max_progress,int progress,boolean indeterminate) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "other_channel");

        // Create notification default intent.
        Intent intent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        builder.setSmallIcon(icon)
                .setContentTitle(title)
                .setContentText(message)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setTicker(message)
                .setChannelId("other_channel")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setProgress(max_progress,progress,indeterminate)
                .setVibrate(new long[0]);

        startForeground(id,builder.build());
    }

    private void uploadImages(final int notification_id, final int index, final List<Images> imagesList, String currentUser_id, String description, ArrayList<String> uploadedImagesUrl) {

        int img_count=index+1;

        Uri imageUri;
        try {
            File compressedFile=new Compressor(this)
                    .setQuality(80)
                    .setCompressFormat(Bitmap.CompressFormat.JPEG)
                    .compressToFile(new File(imagesList.get(index).getPath()));
            imageUri=Uri.fromFile(compressedFile);
        } catch (Exception e) {
            e.printStackTrace();
            imageUri=Uri.fromFile(new File(imagesList.get(index).getPath()));
        }

        final StorageReference fileToUpload=FirebaseStorage.getInstance().getReference().child("post_images").child("FrenzApp_"+System.currentTimeMillis()+"_"+imagesList.get(index).getName());
        fileToUpload.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> fileToUpload.getDownloadUrl()
                        .addOnSuccessListener(uri -> {

                            uploadedImagesUrl.add(uri.toString());
                            int next_index=index+1;
                            try {
                                if (!TextUtils.isEmpty(imagesList.get(index + 1).getOg_path())) {
                                    uploadImages(notification_id,next_index,imagesList,currentUser_id,description, uploadedImagesUrl);
                                } else {
                                    uploadPost(notification_id,currentUser_id,description,uploadedImagesUrl);
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                                uploadPost(notification_id,currentUser_id,description,uploadedImagesUrl);
                            }

                        })
                        .addOnFailureListener(Throwable::printStackTrace))
                .addOnFailureListener(Throwable::printStackTrace)
                .addOnProgressListener(taskSnapshot -> {

                    if(count==1) {
                        String title = "Uploading " + img_count + "/" + imagesList.size() + " images...";
                        int progress = (int) ((100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount());
                        notifyProgress(notification_id
                                , android.R.drawable.stat_sys_upload
                                , title
                                , progress + "%"
                                , getApplicationContext()
                                , 100
                                , progress
                                , false);
                    }else if(count>1){

                        notifyProgress(notification_id
                                , android.R.drawable.stat_sys_upload
                                , "FrenzApp"
                                , "Uploading "+count+" posts"
                                , getApplicationContext()
                                , 100
                                , 0
                                , true);

                    }

                });

    }

    private void uploadPost(int notification_id, String currentUser_id, String description, ArrayList<String> uploadedImagesUrl) {

        if (!uploadedImagesUrl.isEmpty()) {

            if(count==1) {
                notifyProgress(notification_id
                        , android.R.drawable.stat_sys_upload
                        , "FrenzApp"
                        , "Sending post.."
                        , getApplicationContext()
                        , 100
                        , 0
                        , true);
            }
            /*else if(count>1)
            {
                notifyProgress(notification_id+1
                        , android.R.drawable.stat_sys_upload
                        , "FrenzApp"
                        , "Sending post.."
                        , getApplicationContext()
                        , 100
                        , 0
                        , true);
            }
            */

            FirebaseFirestore.getInstance().collection("Users")
                    .document(currentUser_id)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {

                        Map<String,Object> postMap=new HashMap<>();

                        postMap.put("userId", documentSnapshot.getString("id"));
                        postMap.put("username", documentSnapshot.getString("username"));
                        postMap.put("name", documentSnapshot.getString("name"));
                        postMap.put("userimage", documentSnapshot.getString("image"));
                        postMap.put("timestamp", String.valueOf(System.currentTimeMillis()));
                        postMap.put("image_count", uploadedImagesUrl.size());
                        try {
                            postMap.put("image_url_0", uploadedImagesUrl.get(0));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            postMap.put("image_url_1", uploadedImagesUrl.get(1));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            postMap.put("image_url_2", uploadedImagesUrl.get(2));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            postMap.put("image_url_3", uploadedImagesUrl.get(3));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            postMap.put("image_url_4", uploadedImagesUrl.get(4));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            postMap.put("image_url_5", uploadedImagesUrl.get(5));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            postMap.put("image_url_6", uploadedImagesUrl.get(6));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        postMap.put("description",description);
                        postMap.put("color", "0");

                        FirebaseFirestore.getInstance().collection("Posts")
                                .add(postMap)
                                .addOnSuccessListener(documentReference -> {
                                    getSharedPreferences("uploadservice",MODE_PRIVATE)
                                            .edit()
                                            .putInt("count", --count).apply();
                                    Toasty.success(getApplicationContext(), "Post added", Toasty.LENGTH_SHORT, true).show();
                                    if(count==0) {
                                        stopForegroundService(true);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toasty.error(getApplicationContext(),"Error :"+e.getMessage(),Toasty.LENGTH_SHORT,true).show();
                                    stopForegroundService(true);
                                    e.printStackTrace();
                                });


                    }).addOnFailureListener(e -> {
                Toasty.error(getApplicationContext(),"Error :"+e.getMessage(),Toasty.LENGTH_SHORT,true).show();
                stopForegroundService(true);
                e.printStackTrace();
                    });

        } else {
            Toasty.info(this, "No image has been uploaded, Please wait or try again", Toasty.LENGTH_SHORT,true).show();
            stopForegroundService(true);
        }
    }


}
