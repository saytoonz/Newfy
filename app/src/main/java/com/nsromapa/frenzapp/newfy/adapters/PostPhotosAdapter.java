package com.nsromapa.frenzapp.newfy.adapters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.nsromapa.frenzapp.newfy.R;
import com.nsromapa.frenzapp.newfy.models.MultipleImage;
import com.nsromapa.frenzapp.newfy.ui.activities.notification.ImagePreview;
import com.nsromapa.frenzapp.newfy.ui.views.FrenzAppImageView;
import com.nsromapa.frenzapp.newfy.utils.database.UserHelper;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.ivbaranov.mfb.MaterialFavoriteButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import id.zelory.compressor.Compressor;


public class PostPhotosAdapter extends PagerAdapter {


    private ArrayList<MultipleImage> IMAGES;
    private boolean local;
    private LayoutInflater inflater;
    private Context context;
    private File compressedFile;
    private Activity activity;
    private String postId,adminId;
    private static final DecelerateInterpolator DECCELERATE_INTERPOLATOR = new DecelerateInterpolator();
    private static final AccelerateInterpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
    private MaterialFavoriteButton like_btn;

    public PostPhotosAdapter(Context context, Activity activity, ArrayList<MultipleImage> IMAGES, boolean local, String postId, MaterialFavoriteButton like_btn,String adminId) {
        this.context = context;
        this.IMAGES =IMAGES;
        this.local=local;
        this.activity=activity;
        inflater = LayoutInflater.from(context);
        this.postId=postId;
        this.like_btn=like_btn;
        this.adminId=adminId;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return IMAGES.size();
    }


    private void animatePhotoLike(final View vBgLike, final ImageView ivLike) {
        vBgLike.setVisibility(View.VISIBLE);
        ivLike.setVisibility(View.VISIBLE);

        vBgLike.setScaleY(0.1f);
        vBgLike.setScaleX(0.1f);
        vBgLike.setAlpha(1f);
        ivLike.setScaleY(0.1f);
        ivLike.setScaleX(0.1f);

        AnimatorSet animatorSet = new AnimatorSet();

        ObjectAnimator bgScaleYAnim = ObjectAnimator.ofFloat(vBgLike, "scaleY", 0.1f, 1f);
        bgScaleYAnim.setDuration(300);
        bgScaleYAnim.setInterpolator(DECCELERATE_INTERPOLATOR);
        ObjectAnimator bgScaleXAnim = ObjectAnimator.ofFloat(vBgLike, "scaleX", 0.1f, 1f);
        bgScaleXAnim.setDuration(300);
        bgScaleXAnim.setInterpolator(DECCELERATE_INTERPOLATOR);
        ObjectAnimator bgAlphaAnim = ObjectAnimator.ofFloat(vBgLike, "alpha", 1f, 0f);
        bgAlphaAnim.setDuration(300);
        bgAlphaAnim.setStartDelay(150);
        bgAlphaAnim.setInterpolator(DECCELERATE_INTERPOLATOR);

        ObjectAnimator imgScaleUpYAnim = ObjectAnimator.ofFloat(ivLike, "scaleY", 0.1f, 1f);
        imgScaleUpYAnim.setDuration(300);
        imgScaleUpYAnim.setInterpolator(DECCELERATE_INTERPOLATOR);
        ObjectAnimator imgScaleUpXAnim = ObjectAnimator.ofFloat(ivLike, "scaleX", 0.1f, 1f);
        imgScaleUpXAnim.setDuration(300);
        imgScaleUpXAnim.setInterpolator(DECCELERATE_INTERPOLATOR);

        ObjectAnimator imgScaleDownYAnim = ObjectAnimator.ofFloat(ivLike, "scaleY", 1f, 0f);
        imgScaleDownYAnim.setDuration(300);
        imgScaleDownYAnim.setInterpolator(ACCELERATE_INTERPOLATOR);
        ObjectAnimator imgScaleDownXAnim = ObjectAnimator.ofFloat(ivLike, "scaleX", 1f, 0f);
        imgScaleDownXAnim.setDuration(300);
        imgScaleDownXAnim.setInterpolator(ACCELERATE_INTERPOLATOR);

        animatorSet.playTogether(bgScaleYAnim, bgScaleXAnim, bgAlphaAnim, imgScaleUpYAnim, imgScaleUpXAnim);
        animatorSet.play(imgScaleDownYAnim).with(imgScaleDownXAnim).after(imgScaleUpYAnim);

        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                resetLikeAnimationState(vBgLike,ivLike);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                like_btn.setFavorite(true,true);

                Map<String, Object> likeMap = new HashMap<>();
                likeMap.put("liked", true);

                try {

                    FirebaseFirestore.getInstance().collection("Posts")
                            .document(postId)
                            .collection("Liked_Users")
                            .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .set(likeMap)
                            .addOnSuccessListener(aVoid -> {

                                UserHelper userHelper=new UserHelper(context);
                                Cursor rs = userHelper.getData(1);
                                rs.moveToFirst();

                                String image = rs.getString(rs.getColumnIndex(UserHelper.CONTACTS_COLUMN_IMAGE));
                                String username = rs.getString(rs.getColumnIndex(UserHelper.CONTACTS_COLUMN_USERNAME));

                                if (!rs.isClosed()) {
                                    rs.close();
                                }

                                addToNotification(adminId,
                                        FirebaseAuth.getInstance().getCurrentUser().getUid(),
                                        image,
                                        username,
                                        "Liked your post",
                                        postId,
                                        "like");

                            })
                            .addOnFailureListener(e -> Log.e("Error like", e.getMessage()));
                }catch (Exception e){
                    e.printStackTrace();
                }

                /*region new method, for desc check above getLikeandFav in PostsAdapter.java

                Map<String, Object> likeMap = new HashMap<>();
                likeMap.put("liked_users", FieldValue.arrayUnion(FirebaseAuth.getInstance().getCurrentUser().getUid()));
                FirebaseFirestore.getInstance().collection("Posts")
                        .document(postId)
                        .update(likeMap)
                        .addOnSuccessListener(aVoid -> {

                            UserHelper userHelper=new UserHelper(context);
                            Cursor rs = userHelper.getData(1);
                            rs.moveToFirst();

                            String image = rs.getString(rs.getColumnIndex(UserHelper.CONTACTS_COLUMN_IMAGE));
                            String username = rs.getString(rs.getColumnIndex(UserHelper.CONTACTS_COLUMN_USERNAME));

                            if (!rs.isClosed()) {
                                rs.close();
                            }

                            addToNotification(adminId,
                                    FirebaseAuth.getInstance().getCurrentUser().getUid(),
                                    image,
                                    username,
                                    "Liked your post",
                                    postId,
                                    "like");


                        })
                        .addOnFailureListener(e -> Log.e("Error like", e.getMessage()));
                        */

            }
        });
        animatorSet.start();

    }

    private void addToNotification(String admin_id,String user_id,String profile,String username,String message,String post_id,String type){

        Map<String,Object> map=new HashMap<>();
        map.put("id",user_id);
        map.put("username",username);
        map.put("image",profile);
        map.put("message",message);
        map.put("timestamp",String.valueOf(System.currentTimeMillis()));
        map.put("type",type);
        map.put("action_id",post_id);

        if (!admin_id.equals(user_id)) {

            FirebaseFirestore.getInstance().collection("Users")
                    .document(admin_id)
                    .collection("Info_Notifications")
                    .whereEqualTo("id",user_id)
                    .whereEqualTo("action_id",post_id)
                    .whereEqualTo("type",type)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {

                        if(queryDocumentSnapshots.isEmpty()){

                            FirebaseFirestore.getInstance().collection("Users")
                                    .document(admin_id)
                                    .collection("Info_Notifications")
                                    .add(map)
                                    .addOnSuccessListener(documentReference -> {
                                    })
                                    .addOnFailureListener(e -> Log.e("Error", e.getLocalizedMessage()));

                        }

                    })
                    .addOnFailureListener(Throwable::printStackTrace);

        }

    }

    private void resetLikeAnimationState(View vBgLike,ImageView ivLike) {
        vBgLike.setVisibility(View.INVISIBLE);
        ivLike.setVisibility(View.INVISIBLE);
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup view, final int position) {
        final View imageLayout = inflater.inflate(R.layout.item_viewpager_image, view, false);

        assert imageLayout!=null;
        FrenzAppImageView imageView = imageLayout.findViewById(R.id.image);
        final View vBgLike = imageLayout.findViewById(R.id.vBgLike);
        final ImageView ivLike = imageLayout.findViewById(R.id.ivLike);

        if(!local) {

            final GestureDetector detector=new GestureDetector(context, new GestureDetector.SimpleOnGestureListener(){
                @Override
                public boolean onDown(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    super.onLongPress(e);
                    Intent intent=new Intent(context, ImagePreview.class)
                            .putExtra("uri","")
                            //.putExtra("sender_name","Posts")
                            .putExtra("url",IMAGES.get(position).getUrl());
                    context.startActivity(intent);
                }

                @Override
                public boolean onDoubleTap(MotionEvent e) {

                    if(isOnline()) {
                        animatePhotoLike(vBgLike, ivLike);
                    }

                    return true;
                }
            }
            );

            imageView.setOnTouchListener((v, event) -> detector.onTouchEvent(event));


            Glide.with(context)
                    .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.placeholder2))
                    .load(IMAGES.get(position).getUrl())
                    .into(imageView);

        }else{

            try {
                compressedFile= new Compressor(context).setCompressFormat(Bitmap.CompressFormat.PNG).setQuality(75).setMaxHeight(350).compressToFile(new File(IMAGES.get(position).getLocal_path()));
                imageView.setImageURI(Uri.fromFile(compressedFile));


            } catch (IOException e) {
                e.printStackTrace();
            }




        }

        view.addView(imageLayout, 0);

        return imageLayout;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
    }

    @Override
    public Parcelable saveState() {
        return null;
    }


}
