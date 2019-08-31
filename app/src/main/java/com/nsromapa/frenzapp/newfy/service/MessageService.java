package com.nsromapa.frenzapp.newfy.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import id.zelory.compressor.Compressor;

import static com.nsromapa.frenzapp.newfy.utils.Config.random;

public class MessageService extends Service {

    private static final String TAG_FOREGROUND_SERVICE = MessageService.class.getSimpleName();
    public static final String ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE";
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

                int notification_id=intent.getIntExtra("notification_id",1);
                String f_name=intent.getStringExtra("f_name");
                String message=intent.getStringExtra("message");
                String c_name=intent.getStringExtra("c_name");
                String c_image=intent.getStringExtra("c_image");
                String current_id=intent.getStringExtra("current_id");
                String user_id=intent.getStringExtra("user_id");
                String imageUri = intent.getStringExtra("imageUri");
                count=intent.getIntExtra("count",0);

                sendMessage(notification_id,message, Uri.parse(imageUri), c_name, c_image, current_id, user_id, f_name);

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

    private void sendMessage(int notification_id,String message_, Uri imageUri, String c_name, String c_image, String current_id, String user_id,String f_name) {
        //Send message with Image
        StorageReference storageReference=FirebaseStorage.getInstance().getReference()
                .child("notification")
                .child("IMG_"+System.currentTimeMillis()+"_"+random()+".jpg");

        Uri finalUri;
        try {
            File compressedFile=new Compressor(this)
                    .setQuality(80)
                    .setCompressFormat(Bitmap.CompressFormat.JPEG)
                    .compressToFile(new File(imageUri.getPath()));
            finalUri=Uri.fromFile(compressedFile);
        } catch (Exception e) {
            finalUri=imageUri;
            e.printStackTrace();
        }

        storageReference.putFile(finalUri).addOnFailureListener(e -> {
            Toasty.error(getApplicationContext(),"Error :"+e.getMessage(),Toasty.LENGTH_SHORT,true).show();
            NotificationManagerCompat.from(getApplicationContext()).cancel(0);
            e.printStackTrace();
        }).addOnCompleteListener(task -> {

            if(task.isSuccessful()){

                storageReference.getDownloadUrl().addOnSuccessListener(uri -> {

                    Map<String,Object> notificationMessage=new HashMap<>();
                    notificationMessage.put("username",c_name);
                    notificationMessage.put("userimage",c_image);
                    notificationMessage.put("message",message_);
                    notificationMessage.put("from",current_id);
                    notificationMessage.put("notification_id", String.valueOf(System.currentTimeMillis()));
                    notificationMessage.put("timestamp", String.valueOf(System.currentTimeMillis()));
                    notificationMessage.put("read","false");
                    notificationMessage.put("image",uri.toString());

                    FirebaseFirestore.getInstance().collection("Users/"+user_id+"/Notifications_image")
                            .add(notificationMessage)
                            .addOnSuccessListener(documentReference -> {

                                getSharedPreferences("messageservice",MODE_PRIVATE)
                                        .edit()
                                        .putInt("count", --count).apply();

                                Toasty.success(getApplicationContext(),"Message sent to "+f_name,Toasty.LENGTH_SHORT,true).show();

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

            }else{
                Toasty.error(getApplicationContext(),"Error :"+task.getException().getMessage(),Toasty.LENGTH_SHORT,true).show();
                stopForegroundService(true);
            }

        }).addOnProgressListener(taskSnapshot -> {

            //progress in MB
            //String progressText=taskSnapshot.getBytesTransferred()/(1024*1024)+"mb / "+taskSnapshot.getTotalByteCount()/(1024*1024)+"mb";

            if(count==1) {
                notifyProgress(notification_id
                        , android.R.drawable.stat_sys_upload
                        , "Sending image to " + f_name + ".."
                        , ((int) ((100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount())) + "%"
                        , getApplicationContext()
                        , 100
                        , (int) ((100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount())
                        , false);
            }else if (count>1){
                notifyProgress(notification_id
                        , android.R.drawable.stat_sys_upload
                        , "FrenzApp"
                        , "Sending "+count+" images.."
                        , getApplicationContext()
                        , 100
                        , 0
                        , true);
            }

        });

    }

    private void notifyProgress(int id,int icon,String title,String message, Context context,int max_progress,int progress,boolean indeterminate) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "sending_channel");

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
                .setChannelId("sending_channel")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setProgress(max_progress,progress,indeterminate)
                .setVibrate(new long[0]);

        // Show the notification
        //NotificationManagerCompat.from(context).notify(1, builder.build());
        startForeground(id,builder.build());
    }

}
