package com.nsromapa.frenzapp.newfy.ui.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.nsromapa.frenzapp.newfy.R;
import com.nsromapa.frenzapp.newfy.adapters.PostsAdapter;
import com.nsromapa.frenzapp.newfy.models.Post;
import com.nsromapa.frenzapp.newfy.ui.activities.MainActivity;
import com.nsromapa.frenzapp.newfy.ui.activities.notification.ImagePreview;
import com.nsromapa.frenzapp.newfy.utils.AnimationUtil;
import com.nsromapa.frenzapp.newfy.utils.database.UserHelper;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import id.zelory.compressor.Compressor;

import static android.app.Activity.RESULT_OK;
import static androidx.recyclerview.widget.RecyclerView.VERTICAL;

/**
 * Created by SAY on 30/8/19.
 */

public class ProfileFragment extends Fragment {

    View mView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.frag_profile_view, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadFragment(new ProfileFragment.AboutFragment());

        BottomNavigationView bottomNavigationView=mView.findViewById(R.id.bottom_nav);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_profile:
                        loadFragment(new ProfileFragment.AboutFragment());
                        break;
                    case R.id.action_posts:
                        loadFragment(new ProfileFragment.PostsFragment());
                        break;
                    case R.id.action_saved:
                        loadFragment(new ProfileFragment.SavedFragment());
                        break;
                    case R.id.action_edit:
                        loadFragment(new ProfileFragment.EditFragment());
                        break;
                    default:
                        loadFragment(new ProfileFragment.AboutFragment());

                }
                return true;
            }
        });

        bottomNavigationView.setOnNavigationItemReselectedListener(new BottomNavigationView.OnNavigationItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_profile:
                        break;
                    case R.id.action_posts:
                        break;
                    case R.id.action_saved:
                        break;
                    case R.id.action_edit:
                        break;

                }
            }
        });


    }

    private void loadFragment(Fragment fragment) {
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_container, fragment)
                .commit();
    }

    public static class PostsFragment extends Fragment {

        List<Post> postList;
        private RecyclerView mRecyclerView;
        private View statsheetView;
        private SwipeRefreshLayout refreshLayout;
        private TextView title,text;
        private BottomSheetDialog mmBottomSheetDialog;
        private PostsAdapter mAdapter_v19;
        private ImageView image;
        private View rootView;

        public PostsFragment() {
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.main_drawer, container, false);
            return rootView;
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            statsheetView = ((AppCompatActivity)getActivity()).getLayoutInflater().inflate(R.layout.stat_bottom_sheet_dialog, null);
            mmBottomSheetDialog = new BottomSheetDialog(rootView.getContext());
            mmBottomSheetDialog.setContentView(statsheetView);
            mmBottomSheetDialog.setCanceledOnTouchOutside(true);

            refreshLayout=rootView.findViewById(R.id.refreshLayout);

            postList=new ArrayList<>();

            mRecyclerView=rootView.findViewById(R.id.recyclerView);

            title=rootView.findViewById(R.id.default_title);
            text=rootView.findViewById(R.id.default_text);
            image=rootView.findViewById(R.id.imageview);

            image.setImageDrawable(getResources().getDrawable(R.drawable.ic_photo_camera_black_24dp));
            title.setText("No posts found");
            text.setText("Add some posts to see them here");

            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mRecyclerView.setLayoutManager(new LinearLayoutManager(rootView.getContext(), VERTICAL, false));
            mRecyclerView.setHasFixedSize(true);

            mAdapter_v19 = new PostsAdapter(postList, rootView.getContext(), getActivity(), mmBottomSheetDialog, statsheetView, false);
            mRecyclerView.addItemDecoration(new DividerItemDecoration(rootView.getContext(),DividerItemDecoration.VERTICAL));
            mRecyclerView.setAdapter(mAdapter_v19);

            refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    postList.clear();
                    mAdapter_v19.notifyDataSetChanged();
                    getPosts();
                }
            });
            getPosts();

        }

        private void getPosts() {

            refreshLayout.setRefreshing(true);
            rootView.findViewById(R.id.default_item).setVisibility(View.GONE);
            FirebaseFirestore.getInstance().collection("Posts")
                    .whereEqualTo("userId",FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot querySnapshot) {

                            if(!querySnapshot.isEmpty()){

                                for(DocumentChange doc:querySnapshot.getDocumentChanges()){
                                    Post post = doc.getDocument().toObject(Post.class).withId(doc.getDocument().getId());
                                    postList.add(post);
                                    mAdapter_v19.notifyDataSetChanged();
                                    refreshLayout.setRefreshing(false);
                                }

                                if(postList.isEmpty()){
                                    refreshLayout.setRefreshing(false);
                                    rootView.findViewById(R.id.default_item).setVisibility(View.VISIBLE);
                                }

                            }else{
                                refreshLayout.setRefreshing(false);
                                rootView.findViewById(R.id.default_item).setVisibility(View.VISIBLE);
                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            refreshLayout.setRefreshing(false);
                            Toasty.error(rootView.getContext(), "Some technical error occurred", Toasty.LENGTH_SHORT, true).show();
                            Log.e("Error",e.getMessage());
                        }
                    });

        }

    }

    public static class SavedFragment extends Fragment {

        List<Post> postList;
        private RecyclerView mRecyclerView;
        private View statsheetView;
        private BottomSheetDialog mmBottomSheetDialog;
        private SwipeRefreshLayout refreshLayout;
        private PostsAdapter mAdapter_v19;
        private ImageView image;
        private TextView title,text;
        private View rootView;

        public SavedFragment() {
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.main_drawer, container, false);
            return rootView;
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            postList=new ArrayList<>();

            statsheetView = ((AppCompatActivity)getActivity()).getLayoutInflater().inflate(R.layout.stat_bottom_sheet_dialog, null);
            mmBottomSheetDialog = new BottomSheetDialog(rootView.getContext());
            mmBottomSheetDialog.setContentView(statsheetView);
            mmBottomSheetDialog.setCanceledOnTouchOutside(true);
            refreshLayout=rootView.findViewById(R.id.refreshLayout);

            title=rootView.findViewById(R.id.default_title);
            text=rootView.findViewById(R.id.default_text);
            image=rootView.findViewById(R.id.imageview);

            image.setImageDrawable(getResources().getDrawable(R.drawable.ic_bookmark_black_24dp));
            title.setText("No saved posts found");
            text.setText("All your saved posts appear here");

            mRecyclerView=rootView.findViewById(R.id.recyclerView);
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mRecyclerView.setLayoutManager(new LinearLayoutManager(rootView.getContext(), VERTICAL, false));
            mRecyclerView.setHasFixedSize(true);

            mAdapter_v19 = new PostsAdapter(postList, rootView.getContext(), getActivity(), mmBottomSheetDialog, statsheetView, false);
            mRecyclerView.addItemDecoration(new DividerItemDecoration(rootView.getContext(),DividerItemDecoration.VERTICAL));
            mRecyclerView.setAdapter(mAdapter_v19);

            refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {

                    postList.clear();
                    mAdapter_v19.notifyDataSetChanged();
                    getPosts();

                }
            });

            getPosts();

        }

        private void getPosts() {

            rootView.findViewById(R.id.default_item).setVisibility(View.GONE);
            refreshLayout.setRefreshing(true);

            FirebaseFirestore.getInstance().collection("Users")
                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .collection("Saved_Posts")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot querySnapshot) {

                            if(!querySnapshot.isEmpty()){
                                for(final DocumentChange doc:querySnapshot.getDocumentChanges()){

                                    FirebaseFirestore.getInstance().collection("Posts")
                                            .document(doc.getDocument().getId())
                                            .get()
                                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                @Override
                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                    if(documentSnapshot.exists()){
                                                        Post post = doc.getDocument().toObject(Post.class).withId(doc.getDocument().getId());
                                                        postList.add(post);
                                                        mAdapter_v19.notifyDataSetChanged();
                                                        refreshLayout.setRefreshing(false);
                                                    }else{
                                                        FirebaseFirestore.getInstance().collection("Users")
                                                                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                                .collection("Saved_Posts")
                                                                .document(doc.getDocument().getId())
                                                                .delete()
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        refreshLayout.setRefreshing(false);
                                                                        if(postList.isEmpty()) {
                                                                            rootView.findViewById(R.id.default_item).setVisibility(View.VISIBLE);
                                                                        }
                                                                        Log.e("Saved_users","Post not available");
                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        refreshLayout.setRefreshing(false);
                                                                        Toasty.error(rootView.getContext(), "Some technical error occurred", Toasty.LENGTH_SHORT, true).show();
                                                                        Log.e("Error",e.getMessage());
                                                                    }
                                                                });
                                                    }
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    refreshLayout.setRefreshing(false);
                                                    Toasty.error(rootView.getContext(), "Some technical error occurred", Toasty.LENGTH_SHORT, true).show();
                                                    Log.e("Error",e.getMessage());
                                                }
                                            });

                                }
                            }else{
                                refreshLayout.setRefreshing(false);
                                rootView.findViewById(R.id.default_item).setVisibility(View.VISIBLE);
                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            refreshLayout.setRefreshing(false);
                            Toasty.error(rootView.getContext(), "Some technical error occurred", Toasty.LENGTH_SHORT, true).show();
                            Log.e("Error",e.getMessage());
                        }
                    });

        }



    }

    public static class AboutFragment extends Fragment {

        private FirebaseAuth mAuth;
        private FirebaseFirestore mFirestore;
        private UserHelper userHelper;

        private TextView name,username,email,location,post,friend,bio,created;
        private CircleImageView profile_pic;

        public AboutFragment() {
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.frag_about_profile, container, false);


            mAuth = FirebaseAuth.getInstance();
            mFirestore = FirebaseFirestore.getInstance();

            profile_pic=rootView.findViewById(R.id.profile_pic);
            name=rootView.findViewById(R.id.name);
            username=rootView.findViewById(R.id.username);
            email=rootView.findViewById(R.id.email);
            location=rootView.findViewById(R.id.location);
            post=rootView.findViewById(R.id.posts);
            friend=rootView.findViewById(R.id.friends);
            bio=rootView.findViewById(R.id.bio);

            rootView.findViewById(R.id.frame).setVisibility(View.GONE);

            mFirestore.collection("Users")
                    .document(mAuth.getCurrentUser().getUid())
                    .collection("Friends")
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot documentSnapshots) {
                            //Total Friends
                            friend.setText(String.format(Locale.ENGLISH,"Total Friends : %d",documentSnapshots.size()));
                        }
                    });

            userHelper = new UserHelper(rootView.getContext());

            Cursor rs = userHelper.getData(1);
            rs.moveToFirst();

            String usernam=rs.getString(rs.getColumnIndex(UserHelper.CONTACTS_COLUMN_USERNAME));
            String nam = rs.getString(rs.getColumnIndex(UserHelper.CONTACTS_COLUMN_NAME));
            String emai = rs.getString(rs.getColumnIndex(UserHelper.CONTACTS_COLUMN_EMAIL));
            final String imag = rs.getString(rs.getColumnIndex(UserHelper.CONTACTS_COLUMN_IMAGE));
            String loc=rs.getString(rs.getColumnIndex(UserHelper.CONTACTS_COLUMN_LOCATION));
            String bi=rs.getString(rs.getColumnIndex(UserHelper.CONTACTS_COLUMN_BIO));

            if (!rs.isClosed()) {
                rs.close();
            }
            username.setText(String.format(Locale.ENGLISH,"@%s", usernam));
            name.setText(nam);
            email.setText(emai);
            location.setText(loc);
            bio.setText(bi);

            Glide.with(rootView.getContext())
                    .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                    .load(imag)
                    .into(profile_pic);

            profile_pic.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    rootView.getContext().startActivity(new Intent(rootView.getContext(),ImagePreview.class)
                            .putExtra("url",imag));
                    return false;
                }
            });

            FirebaseFirestore.getInstance().collection("Posts")
                    .whereEqualTo("userId",mAuth.getCurrentUser().getUid())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot querySnapshot) {

                            post.setText(String.format(Locale.ENGLISH,"Total Posts : %d",querySnapshot.size()));

                        }
                    });


            return rootView;
        }



    }

    public static class EditFragment extends Fragment {

        private FirebaseAuth mAuth;
        private FirebaseFirestore mFirestore;
        private UserHelper userHelper;

        private TextInputEditText name,username,email,bio,location;
        private CircleImageView profile_pic;
        private TextView updatebtn,updatepicture,updatepassbtn;
        private AuthCredential credential;
        private static final int PICK_IMAGE =100 ;

        private Uri imageUri=null;
        private View rootView;

        public EditFragment() {
        }

        @Override
        public View onCreateView(@NonNull final LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.frag_edit_profile, container, false);

            mAuth = FirebaseAuth.getInstance();
            mFirestore = FirebaseFirestore.getInstance();

            name=rootView.findViewById(R.id.name);
            username=rootView.findViewById(R.id.username);
            email=rootView.findViewById(R.id.email);
            bio=rootView.findViewById(R.id.bio);
            location=rootView.findViewById(R.id.location);
            profile_pic=rootView.findViewById(R.id.profile_pic);
            updatebtn=rootView.findViewById(R.id.update);
            updatepassbtn=rootView.findViewById(R.id.change_password);
            updatepicture=rootView.findViewById(R.id.picture);

            userHelper = new UserHelper(rootView.getContext());

            Cursor rs = userHelper.getData(1);
            rs.moveToFirst();

            final String usernam=rs.getString(rs.getColumnIndex(UserHelper.CONTACTS_COLUMN_USERNAME));
            final String nam = rs.getString(rs.getColumnIndex(UserHelper.CONTACTS_COLUMN_NAME));
            final String emai = rs.getString(rs.getColumnIndex(UserHelper.CONTACTS_COLUMN_EMAIL));
            final String imag = rs.getString(rs.getColumnIndex(UserHelper.CONTACTS_COLUMN_IMAGE));
            final String bi = rs.getString(rs.getColumnIndex(UserHelper.CONTACTS_COLUMN_BIO));
            final String loc = rs.getString(rs.getColumnIndex(UserHelper.CONTACTS_COLUMN_LOCATION));
            final String pass = rs.getString(rs.getColumnIndex(UserHelper.CONTACTS_COLUMN_PASS));

            if (!rs.isClosed()) {
                rs.close();
            }


            updatepicture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(isOnline()){

                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), PICK_IMAGE);

                    }else{
                        Toasty.error(rootView.getContext(), "Some technical error occurred", Toasty.LENGTH_SHORT, true).show();
                    }

                }
            });

            updatepassbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(isOnline()) {

                        new MaterialDialog.Builder(rootView.getContext())
                                .title("Change Password")
                                .content("Enter your old password.")
                                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                                .input("Old password", "", new MaterialDialog.InputCallback() {
                                            @Override
                                            public void onInput(@NonNull MaterialDialog mdialog, CharSequence input) {

                                                if (!input.toString().equals(pass)) {
                                                    mdialog.dismiss();
                                                    Toasty.error(rootView.getContext(), "Invalid password", Toasty.LENGTH_SHORT,true).show();
                                                } else {

                                                    new MaterialDialog.Builder(rootView.getContext())
                                                            .title("Change Password")
                                                            .content("Enter new password.")
                                                            .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                                                            .input("New password", "", new MaterialDialog.InputCallback() {
                                                                @Override
                                                                public void onInput(@NonNull final MaterialDialog mdialog, final CharSequence input) {

                                                                    if(TextUtils.isEmpty(input.toString())){
                                                                        mdialog.dismiss();
                                                                        Toasty.error(rootView.getContext(), "Invalid new password", Toasty.LENGTH_SHORT,true).show();
                                                                    }else if(input.toString().length()<6){
                                                                        Toasty.error(rootView.getContext(), "Password should contain at least 6 characters", Toasty.LENGTH_SHORT,true).show();
                                                                    }else{

                                                                        final ProgressDialog dialog=new ProgressDialog(rootView.getContext());
                                                                        dialog.setMessage("Please wait...");
                                                                        dialog.setIndeterminate(true);
                                                                        dialog.setCancelable(false);
                                                                        dialog.setCanceledOnTouchOutside(false);
                                                                        dialog.show();

                                                                        mAuth.getCurrentUser().updatePassword(input.toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {
                                                                                dialog.dismiss();
                                                                                mdialog.dismiss();
                                                                                userHelper.updateContactPassword(1,input.toString());
                                                                                Toasty.success(rootView.getContext(), "Password updated", Toasty.LENGTH_SHORT,true).show();
                                                                            }
                                                                        }).addOnFailureListener(new OnFailureListener() {
                                                                            @Override
                                                                            public void onFailure(@NonNull Exception e) {
                                                                                dialog.dismiss();
                                                                                mdialog.dismiss();
                                                                                Toasty.error(rootView.getContext(), "Error updating password: "+e.getMessage(), Toasty.LENGTH_SHORT,true).show();
                                                                                Log.e("password error",e.getLocalizedMessage());
                                                                            }
                                                                        });
                                                                    }

                                                                }
                                                            }).show();


                                                }
                                            }
                                        }
                                )
                                .show();

                    }else{
                        Toasty.info(rootView.getContext(), "Go online to change password", Toasty.LENGTH_SHORT,true).show();
                    }
                }
            });

            if(!isOnline()){
                rootView.findViewById(R.id.h_username).animate()
                        .alpha(0.0f)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                rootView.findViewById(R.id.h_username).setVisibility(View.GONE);
                            }
                        }).start();

                rootView.findViewById(R.id.h_email).animate()
                        .alpha(0.0f)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                rootView.findViewById(R.id.h_email).setVisibility(View.GONE);
                            }
                        }).start();

            }

            username.setText(usernam);
            name.setText(nam);
            email.setText(emai);
            bio.setText(bi);
            location.setText(loc);

            Glide.with(rootView.getContext())
                    .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                    .load(imag)
                    .into(profile_pic);

            profile_pic.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    rootView.getContext().startActivity(new Intent(rootView.getContext(),ImagePreview.class)
                            .putExtra("url",imag));
                    return false;
                }
            });

            updatebtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final ProgressDialog dialog=new ProgressDialog(getActivity());
                    dialog.setIndeterminate(true);
                    dialog.setCancelable(false);
                    dialog.setCanceledOnTouchOutside(false);

                    if(isOnline()){

                        final DocumentReference userDocument=mFirestore.collection("Users").document(mAuth.getCurrentUser().getUid());

                        if(imageUri!=null){

                            dialog.setMessage("Updating Details....");
                            dialog.show();

                            final String userUid = mAuth.getCurrentUser().getUid();
                            final StorageReference user_profile = FirebaseStorage.getInstance().getReference().child("images").child(userUid + ".png");
                            user_profile.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {

                                    if(task.isSuccessful()){

                                        user_profile.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(final Uri uri) {
                                                Map<String,Object> map=new HashMap<>();
                                                map.put("image",uri.toString());

                                                userDocument.update(map)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                profile_pic.setImageURI(imageUri);
                                                                MainActivity.imageView.setImageURI(imageUri);
                                                                userHelper.updateContactImage(1,uri.toString());
                                                                dialog.dismiss();
                                                                Log.i("Update","success");
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Log.i("Update","failed: "+e.getMessage());
                                                                dialog.dismiss();
                                                            }
                                                        });
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.e("Error","listen",e);

                                            }
                                        });

                                    }else{
                                        Log.e("Error","listen",task.getException());
                                    }

                                }
                            });

                        }

                        if(!email.getText().toString().equals(emai)){
                            dialog.setMessage("Updating Details....");

                            new MaterialDialog.Builder(rootView.getContext())
                                    .title("Email changed")
                                    .content("It seems that you have changed your email, re-enter your password to change.")
                                    .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                                    .input("Password", "", new MaterialDialog.InputCallback() {
                                        @Override
                                        public void onInput(@NonNull MaterialDialog mdialog, CharSequence input) {
                                            if(!input.toString().equals(pass)){
                                                dialog.dismiss();
                                                mdialog.show();
                                                Toasty.error(rootView.getContext(), "Invalid password", Toasty.LENGTH_SHORT,true).show();
                                            }else{

                                                mdialog.dismiss();
                                                final FirebaseUser currentuser=mAuth.getCurrentUser();

                                                credential = EmailAuthProvider
                                                        .getCredential(currentuser.getEmail(), input.toString());

                                                currentuser.reauthenticate(credential)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                currentuser.updateEmail(email.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                        if (task.isSuccessful()) {

                                                                            currentuser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void aVoid) {

                                                                                    Map<String, Object> userMap = new HashMap<>();
                                                                                    userMap.put("email", email.getText().toString());

                                                                                    FirebaseFirestore.getInstance().collection("Users")
                                                                                            .document(mAuth.getCurrentUser().getUid())
                                                                                            .update(userMap)
                                                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                @Override
                                                                                                public void onSuccess(Void aVoid) {
                                                                                                    dialog.dismiss();
                                                                                                    userHelper.updateContactEmail(1,  email.getText().toString());
                                                                                                    Toasty.success(rootView.getContext(),"Verification email sent.",Toasty.LENGTH_SHORT,true).show();
                                                                                                    dialog.dismiss();
                                                                                                }
                                                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                                        @Override
                                                                                        public void onFailure(@NonNull Exception e) {
                                                                                            dialog.dismiss();
                                                                                            Log.e("Update","failed: "+e.getLocalizedMessage());
                                                                                        }
                                                                                    });

                                                                                }
                                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                                @Override
                                                                                public void onFailure(@NonNull Exception e) {
                                                                                    dialog.dismiss();
                                                                                    Log.e("Error",e.getLocalizedMessage());
                                                                                    dialog.dismiss();
                                                                                }
                                                                            });

                                                                        } else {

                                                                            Log.e("Update email error", task.getException().getMessage() + "..");
                                                                            dialog.dismiss();

                                                                        }

                                                                    }
                                                                });

                                                            }
                                                        });

                                            }
                                        }
                                    })
                                    .positiveText("Done")
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog mdialog, @NonNull DialogAction which) {
                                            dialog.show();
                                            mdialog.dismiss();
                                        }
                                    })
                                    .negativeText("Don't change my email")
                                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog mdialog, @NonNull DialogAction which) {
                                            dialog.dismiss();
                                            mdialog.dismiss();
                                        }
                                    })
                                    .cancelable(false)
                                    .canceledOnTouchOutside(false)
                                    .show();



                        }

                        if(!name.getText().toString().equals(nam)){

                            dialog.setMessage("Updating Details....");
                            dialog.show();

                            Map<String,Object> map=new HashMap<>();
                            map.put("name",name.getText().toString());

                            userDocument.update(map)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            MainActivity.username.setText(nam);
                                            userHelper.updateContactName(1,name.getText().toString());
                                            dialog.dismiss();
                                            Log.i("Update","success");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.i("Update","failed: "+e.getMessage());
                                            dialog.dismiss();
                                        }
                                    });

                        }

                        if(!username.getText().toString().equals(usernam)){

                            dialog.setMessage("Updating Details....");
                            dialog.show();

                            mFirestore.collection("Usernames")
                                    .document(username.getText().toString())
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            if(!documentSnapshot.exists()){

                                                mFirestore.collection("Usernames")
                                                        .document(usernam)
                                                        .delete()
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {

                                                                Map<String,Object> map=new HashMap<>();
                                                                map.put("username",username.getText().toString());

                                                                mFirestore.collection("Usernames")
                                                                        .document(username.getText().toString())
                                                                        .set(map)
                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {

                                                                                Map<String,Object> map=new HashMap<>();
                                                                                map.put("username",username.getText().toString());

                                                                                userDocument.update(map)
                                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                            @Override
                                                                                            public void onSuccess(Void aVoid) {
                                                                                                dialog.dismiss();
                                                                                                userHelper.updateContactUserName(1,username.getText().toString());
                                                                                                Log.i("Update","success");
                                                                                            }
                                                                                        })
                                                                                        .addOnFailureListener(new OnFailureListener() {
                                                                                            @Override
                                                                                            public void onFailure(@NonNull Exception e) {
                                                                                                dialog.dismiss();
                                                                                                Log.i("Update","failed: "+e.getMessage());

                                                                                            }
                                                                                        });

                                                                            }
                                                                        })
                                                                        .addOnFailureListener(new OnFailureListener() {
                                                                            @Override
                                                                            public void onFailure(@NonNull Exception e) {
                                                                                dialog.dismiss();
                                                                                Log.i("error","failed: "+e.getMessage());
                                                                            }
                                                                        });

                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                dialog.dismiss();
                                                                Log.i("error","failed: "+e.getMessage());

                                                            }
                                                        });



                                            }else{

                                                dialog.dismiss();
                                                Toasty.error(rootView.getContext(), "Username already exists", Toasty.LENGTH_SHORT,true).show();
                                                AnimationUtil.shakeView(username,rootView.getContext());

                                            }
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            dialog.dismiss();
                                            Log.e("error",""+e.getLocalizedMessage());
                                        }
                                    });

                        }

                        if(!bio.getText().toString().equals(bi)){

                            dialog.setMessage("Updating Details....");
                            dialog.show();

                            Map<String,Object> map=new HashMap<>();
                            map.put("bio",bio.getText().toString());

                            userDocument.update(map)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            dialog.dismiss();
                                            userHelper.updateContactBio(1,bio.getText().toString());
                                            Log.i("Update","success");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            dialog.dismiss();
                                            Log.i("Update","failed: "+e.getMessage());

                                        }
                                    });

                        }

                        if(!location.getText().toString().equals(loc)){

                            dialog.setMessage("Updating Details....");
                            dialog.show();

                            Map<String,Object> map=new HashMap<>();
                            map.put("location",location.getText().toString());

                            userDocument.update(map)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            dialog.dismiss();
                                            userHelper.updateContactLocation(1,location.getText().toString());
                                            Log.i("Update","success");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            dialog.dismiss();
                                            Log.i("Update","failed: "+e.getMessage());

                                        }
                                    });

                        }


                    }else{


                        if(!name.getText().toString().equals(nam)){

                            userHelper.updateContactName(1,name.getText().toString());

                        }

                        if(!bio.getText().toString().equals(bi)){

                            userHelper.updateContactBio(1,bio.getText().toString());

                        }

                        if(!location.getText().toString().equals(loc)){

                            userHelper.updateContactLocation(1,location.getText().toString());

                        }

                        Toasty.info(rootView.getContext(),"Only your name,bio and location has been scheduled for update, when internet connection is available it will be updated.",Toasty.LENGTH_LONG).show();


                    }

                }
            });

            return rootView;
        }

        public boolean isOnline() {
            ConnectivityManager cm =
                    (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnectedOrConnecting();
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            if(requestCode==PICK_IMAGE){
                if(resultCode==RESULT_OK){
                    imageUri=data.getData();
                    // start crop activity
                    UCrop.Options options = new UCrop.Options();
                    options.setCompressionFormat(Bitmap.CompressFormat.PNG);
                    options.setCompressionQuality(100);
                    options.setShowCropGrid(true);

                    UCrop.of(imageUri, Uri.fromFile(new File(rootView.getContext().getCacheDir(), "hify_user_profile_picture.png")))
                            .withAspectRatio(1, 1)
                            .withOptions(options)
                            .start(getActivity());

                }
            }
            if (requestCode == UCrop.REQUEST_CROP) {
                if (resultCode == RESULT_OK) {
                    try {
                        File compressedFile= new Compressor(rootView.getContext()).setCompressFormat(Bitmap.CompressFormat.PNG).setQuality(70).setMaxHeight(96).setMaxWidth(96).compressToFile(new File(UCrop.getOutput(data).getPath()));
                        profile_pic.setImageURI(Uri.fromFile(compressedFile));
						imageUri=Uri.fromFile(compressedFile);
                        Toasty.info(rootView.getContext(), "Profile picture uploaded, click Save details button to apply changes", Toasty.LENGTH_LONG,true).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toasty.info(rootView.getContext(), "Profile photo updated click Save details to apply but unable to compress: "+e.getLocalizedMessage(), Toasty.LENGTH_SHORT,true).show();
                        profile_pic.setImageURI(imageUri);
                        imageUri = UCrop.getOutput(data);
                    }
                } else if (resultCode == UCrop.RESULT_ERROR) {
                    Log.e("Error", "Crop error:" + UCrop.getError(data).getMessage());
                }
            }

        }
    }

}
