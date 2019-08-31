package com.nsromapa.frenzapp.newfy.ui.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nsromapa.frenzapp.newfy.R;
import com.nsromapa.frenzapp.newfy.adapters.PostsAdapter;
import com.nsromapa.frenzapp.newfy.models.Post;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

/**
 * Created by SAY on 30/8/19.
 */

public class Feeds extends Fragment {

    private List<Post> mPostsList;
    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private RecyclerView mPostsRecyclerView;
    private View mView;
    private List<String> mFriendIdList=new ArrayList<>();
    private View statsheetView;
    private BottomSheetDialog mmBottomSheetDialog;
    private SwipeRefreshLayout refreshLayout;
    private PostsAdapter mAdapter_v19;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.frag_home, container, false);
        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();
        mAdapter_v19.notifyDataSetChanged();

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        refreshLayout=view.findViewById(R.id.refreshLayout);

        statsheetView = ((AppCompatActivity)getActivity()).getLayoutInflater().inflate(R.layout.stat_bottom_sheet_dialog, null);
        mmBottomSheetDialog = new BottomSheetDialog(view.getContext());
        mmBottomSheetDialog.setContentView(statsheetView);
        mmBottomSheetDialog.setCanceledOnTouchOutside(true);
        mPostsRecyclerView = view.findViewById(R.id.posts_recyclerview);

        mPostsList = new ArrayList<>();

        mAdapter_v19 = new PostsAdapter(mPostsList, view.getContext(), getActivity(), mmBottomSheetDialog, statsheetView, false);
        mPostsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mPostsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mPostsRecyclerView.setHasFixedSize(true);
        mPostsRecyclerView.addItemDecoration(new DividerItemDecoration(view.getContext(),DividerItemDecoration.VERTICAL));
        mPostsRecyclerView.setAdapter(mAdapter_v19);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                mPostsList.clear();
                mAdapter_v19.notifyDataSetChanged();
                getAllPosts();

            }
        });

        getAllPosts();
    }

    private void getAllPosts() {

        mView.findViewById(R.id.default_item).setVisibility(View.GONE);
        refreshLayout.setRefreshing(true);

        mFirestore.collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if (!queryDocumentSnapshots.isEmpty()) {

                            for (final DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                                if (doc.getType() == DocumentChange.Type.ADDED) {

                                    mFirestore.collection("Users")
                                            .document(currentUser.getUid())
                                            .collection("Friends")
                                            .get()
                                            .addOnSuccessListener(querySnapshot -> {

                                                if (!querySnapshot.isEmpty()) {

                                                    for (DocumentChange documentChange : querySnapshot.getDocumentChanges()) {
                                                        if (documentChange.getDocument().getId().equals(doc.getDocument().get("userId"))) {

                                                            Post post = doc.getDocument().toObject(Post.class).withId(doc.getDocument().getId());
                                                            mPostsList.add(post);
                                                            refreshLayout.setRefreshing(false);
                                                            mAdapter_v19.notifyDataSetChanged();

                                                        }
                                                    }

                                                } else {

                                                   getCurrentUsersPosts();

                                                }

                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    refreshLayout.setRefreshing(false);
                                                    Toasty.error(mView.getContext(), "Some technical error occurred", Toasty.LENGTH_SHORT,true).show();
                                                    Log.w("Error", "listen:error", e);
                                                }
                                            });

                                }
                            }


                        }else{
                            refreshLayout.setRefreshing(false);
                            mView.findViewById(R.id.default_item).setVisibility(View.VISIBLE);

                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        refreshLayout.setRefreshing(false);
                        Toasty.error(mView.getContext(), "Some technical error occurred", Toasty.LENGTH_SHORT,true).show();
                        Log.w("Error", "listen:error", e);
                    }
                });

    }

    private void getCurrentUsersPosts() {

        mFirestore.collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if(queryDocumentSnapshots.isEmpty()){

                            refreshLayout.setRefreshing(false);
                            mView.findViewById(R.id.default_item).setVisibility(View.VISIBLE);

                        }else{

                            for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
                                if (documentChange.getDocument().getString("userId").equals(mAuth.getCurrentUser().getUid())) {

                                    Post post = documentChange.getDocument().toObject(Post.class).withId(documentChange.getDocument().getId());
                                    mPostsList.add(post);
                                    refreshLayout.setRefreshing(false);
                                    mAdapter_v19.notifyDataSetChanged();

                                }
                            }

                            if (mPostsList.isEmpty()) {
                                mView.findViewById(R.id.default_item).setVisibility(View.VISIBLE);
                                refreshLayout.setRefreshing(false);
                            }

                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        refreshLayout.setRefreshing(false);
                        Toasty.error(mView.getContext(), "Some technical error occurred", Toasty.LENGTH_SHORT,true).show();
                        Log.w("Error", "listen:error", e);
                    }
                });


    }

}
