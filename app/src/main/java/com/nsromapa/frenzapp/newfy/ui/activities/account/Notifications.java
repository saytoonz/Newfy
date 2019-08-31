package com.nsromapa.frenzapp.newfy.ui.activities.account;

import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.nsromapa.frenzapp.newfy.R;
import com.nsromapa.frenzapp.newfy.adapters.NotificationsAdapter;
import com.nsromapa.frenzapp.newfy.models.Notification;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.marcoscg.dialogsheet.DialogSheet;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

import static androidx.recyclerview.widget.RecyclerView.VERTICAL;

/**
 * Created by SAY on 30/8/19.
 */

public class Notifications extends AppCompatActivity {

    private List<Notification> notificationsList;
    private NotificationsAdapter notificationsAdapter;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout refreshLayout;

    public void getNotifications() {

        findViewById(R.id.default_item).setVisibility(View.GONE);
        notificationsList.clear();
        refreshLayout.setRefreshing(true);
        FirebaseFirestore.getInstance().collection("Users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("Info_Notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    getSharedPreferences("Notifications",MODE_PRIVATE).edit().putInt("count",queryDocumentSnapshots.size()).apply();

                    if(!queryDocumentSnapshots.isEmpty()){

                        for(DocumentChange documentChange:queryDocumentSnapshots.getDocumentChanges()){

                            if(documentChange.getType()== DocumentChange.Type.ADDED){

                                refreshLayout.setRefreshing(false);
                                Notification notification=documentChange.getDocument().toObject(Notification.class).withId(documentChange.getDocument().getId());
                                notificationsList.add(notification);
                                notificationsAdapter.notifyDataSetChanged();

                            }

                        }

                        if(notificationsList.size()==0){
                            refreshLayout.setRefreshing(false);
                            findViewById(R.id.default_item).setVisibility(View.VISIBLE);
                        }

                    }else{
                        refreshLayout.setRefreshing(false);
                        findViewById(R.id.default_item).setVisibility(View.VISIBLE);
                    }

                })
                .addOnFailureListener(e -> {
                    refreshLayout.setRefreshing(false);
                    e.printStackTrace();
                });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_notifications, menu);

        return super.onCreateOptionsMenu(menu);    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clear:

                new DialogSheet(this)
                        .setTitle("Clear all")
                        .setMessage("Are you sure do you want to clear all notifications?")
                        .setRoundedCorners(true)
                        .setColoredNavigationBar(true)
                        .setCancelable(true)
                        .setPositiveButton("Yes", v -> {

                            deleteAll();

                        })
                        .setNegativeButton("No", v -> {

                        })
                        .show();

               return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteAll() {


        refreshLayout.setRefreshing(true);
        FirebaseFirestore.getInstance().collection("Users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("Info_Notifications")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if(!queryDocumentSnapshots.isEmpty()){

                            for(DocumentChange documentChange:queryDocumentSnapshots.getDocumentChanges()){

                                refreshLayout.setRefreshing(true);
                                FirebaseFirestore.getInstance().collection("Users")
                                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .collection("Info_Notifications")
                                        .document(documentChange.getDocument().getId())
                                        .delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                refreshLayout.setRefreshing(false);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                e.printStackTrace();
                                            }
                                        });

                            }

                            notificationsList.clear();
                            findViewById(R.id.default_item).setVisibility(View.VISIBLE);
                            Toasty.success(getApplicationContext(),"Notifications cleared",Toasty.LENGTH_SHORT,true).show();


                        }else{
                            findViewById(R.id.default_item).setVisibility(View.VISIBLE);
                            refreshLayout.setRefreshing(false);
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        refreshLayout.setRefreshing(false);
                        e.printStackTrace();
                    }
                });

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/bold.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build());
        setContentView(R.layout.activity_notifications);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mRecyclerView = findViewById(R.id.recyclerView);
        refreshLayout=findViewById(R.id.refreshLayout);

        notificationsList = new ArrayList<>();
        notificationsAdapter = new NotificationsAdapter(notificationsList, this);

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, VERTICAL, false));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(notificationsAdapter);

        refreshLayout.setOnRefreshListener(() -> getNotifications());

        getNotifications();

    }
}
