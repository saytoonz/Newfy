package com.nsromapa.frenzapp.newfy.ui.activities.post;

import android.content.Context;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nsromapa.frenzapp.newfy.R;
import com.nsromapa.frenzapp.newfy.adapters.PostsAdapter;
import com.nsromapa.frenzapp.newfy.models.Post;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class SinglePostView extends AppCompatActivity {

    private List<Post> mPostsList;
    private PostsAdapter mAdapter;
    private View statsheetView;
    private BottomSheetDialog mmBottomSheetDialog;
    private ProgressBar pbar;
    private FirebaseFirestore mFirestore;


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
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

        setContentView(R.layout.activity_single_post_view);

        String post_id=getIntent().getStringExtra("post_id");

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Post");

        getSupportActionBar().setTitle("Post");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if(!TextUtils.isEmpty(post_id)){

            boolean forComment=getIntent().getBooleanExtra("forComment",false);

            pbar=findViewById(R.id.pbar);
            mFirestore=FirebaseFirestore.getInstance();

            statsheetView = getLayoutInflater().inflate(R.layout.stat_bottom_sheet_dialog, null);
            mmBottomSheetDialog = new BottomSheetDialog(this);
            mmBottomSheetDialog.setContentView(statsheetView);
            mmBottomSheetDialog.setCanceledOnTouchOutside(true);

            mPostsList = new ArrayList<>();

            if(forComment)
                mAdapter = new PostsAdapter(mPostsList, this,this,mmBottomSheetDialog,statsheetView,true);
            else
                mAdapter = new PostsAdapter(mPostsList, this,this,mmBottomSheetDialog,statsheetView,false);


            RecyclerView mRecyclerView=findViewById(R.id.recyclerView);
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.setAdapter(mAdapter);

            pbar.setVisibility(View.VISIBLE);
            getPosts(post_id);



        }else{
            finish();
        }

    }

    private void getPosts(final String post_id) {

        mFirestore.collection("Posts")
                .document(post_id)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(!documentSnapshot.exists()){
                            Toasty.error(SinglePostView.this, "The post does not exist.", Toasty.LENGTH_SHORT, true).show();
                        }else{

                            Post post = new Post(documentSnapshot.getString("userId"),documentSnapshot.getString("name"),documentSnapshot.getString("timestamp"),documentSnapshot.getString("likes"),documentSnapshot.getString("favourites"),documentSnapshot.getString("description"),documentSnapshot.getString("color"),documentSnapshot.getString("username"),documentSnapshot.getString("userimage"),Integer.parseInt(String.valueOf(documentSnapshot.get("image_count"))),documentSnapshot.getString("image_url_0"),documentSnapshot.getString("image_url_1"),documentSnapshot.getString("image_url_2"),documentSnapshot.getString("image_url_3"),documentSnapshot.getString("image_url_4"),documentSnapshot.getString("image_url_5"),documentSnapshot.getString("image_url_6")).withId(post_id);
                            mPostsList.add(post);
                            mAdapter.notifyDataSetChanged();
                            pbar.setVisibility(View.GONE);

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        Toasty.error(SinglePostView.this, "Some error occured opening the post", Toasty.LENGTH_SHORT,true).show();
                        finish();
                    }
                });

    }
}
