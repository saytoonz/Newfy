package com.nsromapa.frenzapp.newfy.receivers;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.nsromapa.frenzapp.newfy.ui.activities.forum.AnswersActivity;
import com.nsromapa.frenzapp.newfy.ui.activities.account.UpdateAvailable;
import com.nsromapa.frenzapp.newfy.ui.activities.MainActivity;
import com.nsromapa.frenzapp.newfy.ui.activities.friends.FriendProfile;
import com.nsromapa.frenzapp.newfy.ui.activities.notification.NotificationActivity;
import com.nsromapa.frenzapp.newfy.ui.activities.notification.NotificationImage;
import com.nsromapa.frenzapp.newfy.ui.activities.notification.NotificationImageReply;
import com.nsromapa.frenzapp.newfy.ui.activities.notification.NotificationReplyActivity;
import com.nsromapa.frenzapp.newfy.ui.activities.post.CommentsActivity;
import com.nsromapa.frenzapp.newfy.ui.activities.post.SinglePostView;
import com.nsromapa.frenzapp.newfy.utils.Config;
import com.nsromapa.frenzapp.newfy.utils.NotificationUtil;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by SAY on 30/8/19.
 */

public class FCMService extends FirebaseMessagingService {

    private static final String TAG = FCMService.class.getSimpleName();

    private NotificationUtil notificationUtils;

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d("NEW_TOKEN",token);

        storeRegIdInPref(token);

        Intent registrationComplete = new Intent(Config.REGISTRATION_COMPLETE);
        registrationComplete.putExtra("token", token);

        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    private void storeRegIdInPref(String token) {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("regId", token);
        editor.apply();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        handleDataMessage(remoteMessage);
    }

    private void handleDataMessage(RemoteMessage remoteMessage) {

        final String title    = remoteMessage.getData().get("title");
        final String body     = remoteMessage.getData().get("body");
        String click_action   = remoteMessage.getData().get("click_action");
        String message        = remoteMessage.getData().get("message");
        String from_name      = remoteMessage.getData().get("from_name");
        String from_image     = remoteMessage.getData().get("from_image");
        String from_id        = remoteMessage.getData().get("from_id");
        final String imageUrl = remoteMessage.getData().get("image");
        String reply_for      = remoteMessage.getData().get("reply_for");
        String timeStamp      = String.valueOf(remoteMessage.getData().get("timestamp"));

        //Friend Request Message data
        String friend_id    = remoteMessage.getData().get("friend_id");
        String friend_name  = remoteMessage.getData().get("friend_name");
        String friend_email = remoteMessage.getData().get("friend_email");
        String friend_image = remoteMessage.getData().get("friend_image");

        //CommentData and Like Data
        String post_id   = remoteMessage.getData().get("post_id");
        String admin_id  = remoteMessage.getData().get("admin_id");
        String post_desc = remoteMessage.getData().get("post_desc");
        String channel   = remoteMessage.getData().get("channel");

        //UpdateData
        String version      = remoteMessage.getData().get("version");
        String improvements = remoteMessage.getData().get("improvements");
        String link         = remoteMessage.getData().get("link");

        String question_id=remoteMessage.getData().get("question_id");

        String notification_type=remoteMessage.getData().get("notification_type");

        final Intent resultIntent;

        switch (click_action) {
            case "com.nsromapa.frenzapp.TARGETNOTIFICATION":

                resultIntent = new Intent(getApplicationContext(), NotificationActivity.class);

                break;
            case "com.nsromapa.frenzapp.TARGETNOTIFICATIONREPLY":

                resultIntent = new Intent(getApplicationContext(), NotificationReplyActivity.class);

                break;
            case "com.nsromapa.frenzapp.TARGETNOTIFICATION_IMAGE":

                resultIntent = new Intent(getApplicationContext(), NotificationImage.class);

                break;
            case "com.nsromapa.frenzapp.TARGETNOTIFICATIONREPLY_IMAGE":

                resultIntent = new Intent(getApplicationContext(), NotificationImageReply.class);

                break;
            case "com.nsromapa.frenzapp.TARGET_FRIENDREQUEST":

                Log.d("TARGET_FRIENDREQUEST", "onMessageReceived: " + click_action);
                resultIntent = new Intent(getApplicationContext(), FriendProfile.class);

                break;
            case "com.nsromapa.frenzapp.TARGET_ACCEPTED":

                Log.d("TARGET_ACCEPTED", "onMessageReceived: " + click_action);
                resultIntent = new Intent(getApplicationContext(), FriendProfile.class);

                break;
            case "com.nsromapa.frenzapp.TARGET_DECLINED":

                Log.d("TARGET_DECLINED", "onMessageReceived: " + click_action);
                resultIntent = new Intent(getApplicationContext(), FriendProfile.class);

                break;
            case "com.nsromapa.frenzapp.TARGET_COMMENT":


                Log.d("TARGET_COMMENT", "onMessageReceived: " + click_action);
                resultIntent = new Intent(getApplicationContext(), CommentsActivity.class);

                break;
            case "com.nsromapa.frenzapp.TARGET_UPDATE":

                resultIntent = new Intent(getApplicationContext(), UpdateAvailable.class);

                break;
            case "com.nsromapa.frenzapp.TARGET_LIKE":

                Log.d("TARGET_LIKE", "onMessageReceived: " + click_action);
                resultIntent = new Intent(getApplicationContext(), SinglePostView.class);

                break;
            case "com.nsromapa.frenzapp.TARGET_FORUM":

                Log.d("TARGET_FORUM", "onMessageReceived: " + click_action);
                resultIntent = new Intent(getApplicationContext(), AnswersActivity.class);

                break;
            default:

                resultIntent = new Intent(getApplicationContext(), MainActivity.class);

                break;
        }

        resultIntent.putExtra("title", title);
        resultIntent.putExtra("body", body);
        resultIntent.putExtra("name", from_name);
        resultIntent.putExtra("from_image", from_image);
        resultIntent.putExtra("message", message);
        resultIntent.putExtra("from_id", from_id);
        resultIntent.putExtra("timestamp", timeStamp);
        resultIntent.putExtra("reply_for", reply_for);
        resultIntent.putExtra("image", imageUrl);
        resultIntent.putExtra("reply_image", from_image);

        resultIntent.putExtra("f_id", friend_id);
        resultIntent.putExtra("f_name", friend_name);
        resultIntent.putExtra("f_email", friend_email);
        resultIntent.putExtra("f_image", friend_image);

        resultIntent.putExtra("user_id", admin_id);
        resultIntent.putExtra("post_id", post_id);
        resultIntent.putExtra("post_desc", post_desc);

        resultIntent.putExtra("channel", channel);
        resultIntent.putExtra("version", version);
        resultIntent.putExtra("improvements", improvements);
        resultIntent.putExtra("link", link);

        resultIntent.putExtra("question_id",question_id);

        resultIntent.putExtra("notification_type",notification_type);

        try {

            boolean foreground = new ForegroundCheckTask().execute(getApplicationContext()).get();

            if(!foreground){

                // check for image attachment
                if (TextUtils.isEmpty(imageUrl)) {


                    if (!TextUtils.isEmpty(from_image)) {

                        showNotificationMessage(timeStamp, from_image, getApplicationContext(), title, body, resultIntent, notification_type);
                    } else {

                        showNotificationMessage(timeStamp, friend_image, getApplicationContext(), title, body, resultIntent, notification_type);
                    }

                } else {

                    // image is present, show notification with image
                    if (!TextUtils.isEmpty(from_image)) {
                        showNotificationMessageWithBigImage(timeStamp,from_image, getApplicationContext(), title, body, resultIntent, imageUrl, notification_type);
                    } else {
                        showNotificationMessageWithBigImage(timeStamp, friend_image, getApplicationContext(), title, body, resultIntent, imageUrl, notification_type);
                    }
                }

            }else{

                boolean active = getSharedPreferences("fcm_activity",MODE_PRIVATE).getBoolean("active",true);

                if(active) {

                    Log.d("FCM_LOGIC", "AFTER if(active) ");

                    Intent intent = new Intent(Config.PUSH_NOTIFICATION);

                    intent.putExtra("title", title);
                    intent.putExtra("body", body);
                    intent.putExtra("name", from_name);
                    intent.putExtra("from_image", from_image);
                    intent.putExtra("message", message);
                    intent.putExtra("from_id", from_id);
                    intent.putExtra("timestamp", timeStamp);
                    intent.putExtra("reply_for", reply_for);
                    intent.putExtra("image", imageUrl);
                    intent.putExtra("reply_image", from_image);

                    intent.putExtra("f_id", friend_id);
                    intent.putExtra("f_name", friend_name);
                    intent.putExtra("f_email", friend_email);
                    intent.putExtra("f_image", friend_image);

                    intent.putExtra("user_id", admin_id);
                    intent.putExtra("post_id", post_id);
                    intent.putExtra("post_desc", post_desc);
                    intent.putExtra("click_action", click_action);

                    intent.putExtra("version", version);
                    intent.putExtra("improvements", improvements);
                    intent.putExtra("link", link);
                    intent.putExtra("channel",channel);
                    intent.putExtra("question_id",question_id);

                    resultIntent.putExtra("notification_type",notification_type);

                    if (title.toLowerCase().contains("update")) {

                        Log.d("FCM_LOGIC", "showNotificationMessage if(title.toLowerCase().contains(updatePer)) ");
                        showNotificationMessage(timeStamp, from_image, getApplicationContext(), title, body, resultIntent,notification_type);
                        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                    } else {

                        Log.d("FCM_LOGIC", "showNotificationMessage if(title.toLowerCase().contains(updatePer)) ELSE ");
                        showNotificationMessage(timeStamp, from_image, getApplicationContext(), title, body, resultIntent,notification_type);
                        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                    }
                }else{

                    Log.d("FCM_LOGIC", "AFTER if(active) ELSE");

                    if (TextUtils.isEmpty(imageUrl)) {

                        Log.d("FCM_LOGIC", "AFTER if(TextUtils.isEmpty(imageUrl)) ");

                        if (!TextUtils.isEmpty(from_image)) {

                            Log.d("FCM_LOGIC", "showNotificationMessage AFTER if(!TextUtils.isEmpty(from_image)) ");
                            showNotificationMessage(timeStamp, from_image, getApplicationContext(), title, body, resultIntent,notification_type);
                        } else {

                            Log.d("FCM_LOGIC", "showNotificationMessage AFTER if(TextUtils.isEmpty(from_image)) ELSE ");
                            showNotificationMessage(timeStamp, friend_image, getApplicationContext(), title, body, resultIntent,notification_type);
                        }

                    } else {

                        Log.d("FCM_LOGIC", "AFTER if(TextUtils.isEmpty(imageUrl)) ELSE ");
                        // image is present, show notification with image
                        if (!TextUtils.isEmpty(from_image)) {

                            Log.d("FCM_LOGIC", "showNotificationMessageWithBigImage AFTER if(!TextUtils.isEmpty(from_image)) ");
                            showNotificationMessageWithBigImage(timeStamp, from_image, getApplicationContext(), title, body, resultIntent, imageUrl,notification_type);
                        } else {

                            Log.d("FCM_LOGIC", "showNotificationMessageWithBigImage AFTER if(!TextUtils.isEmpty(from_image)) ELSE ");
                            showNotificationMessageWithBigImage(timeStamp, friend_image, getApplicationContext(), title, body, resultIntent, imageUrl,notification_type);
                        }
                    }


                }

            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }



    }

    private void showNotificationMessage(String timeStamp, String user_image, Context context, String title, String message, Intent intent, String notification_type) {
        notificationUtils = new NotificationUtil(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(timeStamp, user_image, title, message, intent, null,notification_type);
    }

    private void showNotificationMessageWithBigImage(String timeStamp, String user_image, Context context, String title, String message, Intent intent, String imageUrl, String notification_type) {
        notificationUtils = new NotificationUtil(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(timeStamp, user_image, title, message, intent, imageUrl,notification_type);
    }

    private class ForegroundCheckTask extends AsyncTask<Context, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Context... params) {
            final Context context = params[0].getApplicationContext();
            return isAppOnForeground(context);
        }

        private boolean isAppOnForeground(Context context) {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
            if (appProcesses == null) {
                return false;
            }
            final String packageName = context.getPackageName();
            for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                    return true;
                }
            }
            return false;
        }
    }

}
