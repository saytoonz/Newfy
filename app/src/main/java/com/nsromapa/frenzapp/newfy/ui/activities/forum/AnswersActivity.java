package com.nsromapa.frenzapp.newfy.ui.activities.forum;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.nsromapa.frenzapp.newfy.R;
import com.nsromapa.frenzapp.newfy.adapters.AnswersAdapter;
import com.nsromapa.frenzapp.newfy.models.Answers;
import com.nsromapa.frenzapp.newfy.utils.database.UserHelper;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import es.dmoral.toasty.Toasty;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;


public class AnswersActivity extends AppCompatActivity {

    private static final String TAG = AnswersActivity.class.getSimpleName();
    String author_id,author,question,timestamp,doc_id;
    TextView author_textview,question_textview;
    EditText answer;
    RecyclerView mRecyclerView;
    FirebaseFirestore mFirestore;
    FirebaseUser mCurrentUser;
    AnswersAdapter adapter;
    List<Answers> answers=new ArrayList<>();
    private String answered_by;
    private Toolbar toolbar;
    private SwipeRefreshLayout refreshLayout;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(base));
    }

    public static void startActivity(Context context,String question_id){
        context.startActivity(new Intent(context,AnswersActivity.class).putExtra("question_id",question_id));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private void addToNotification(String admin_id, String user_id, String profile, String username, String message, String question_id, String type, ProgressDialog mDialog){

        Map<String,Object> map=new HashMap<>();
        map.put("id",user_id);
        map.put("username",username);
        map.put("image",profile);
        map.put("message",message);
        map.put("timestamp",String.valueOf(System.currentTimeMillis()));
        map.put("type",type);
        map.put("action_id",question_id);

        if(!admin_id.equals(user_id)) {

            mFirestore.collection("Users")
                    .document(admin_id)
                    .collection("Info_Notifications")
                    .add(map)
                    .addOnSuccessListener(documentReference -> {
                        mDialog.dismiss();
                        answer.setText("");
                        getAnswers();
                        Toasty.success(AnswersActivity.this, "Answer added", Toasty.LENGTH_SHORT, true).show();
                        adapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> Log.e("Error", e.getLocalizedMessage()));

        }else{
            mDialog.dismiss();
            answer.setText("");
            getAnswers();
            Toasty.success(AnswersActivity.this, "Answer added", Toasty.LENGTH_SHORT, true).show();
            adapter.notifyDataSetChanged();
        }
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

        setContentView(R.layout.activity_answers);

        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Forum");

        getSupportActionBar().setTitle("Forum");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mFirestore=FirebaseFirestore.getInstance();
        mCurrentUser= FirebaseAuth.getInstance().getCurrentUser();
        mRecyclerView=findViewById(R.id.recyclerView);
        refreshLayout=findViewById(R.id.refreshLayout);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        if(StringUtils.isNotEmpty(getIntent().getStringExtra("question_id"))){

            Log.i(TAG,getIntent().getStringExtra("question_id"));

            mFirestore.collection("Questions")
                    .document(getIntent().getStringExtra("question_id"))
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {

                        if(documentSnapshot.exists()) {

                            author_id = documentSnapshot.getString("id");
                            author = documentSnapshot.getString("name");
                            doc_id = documentSnapshot.getId();
                            timestamp = documentSnapshot.getString("timestamp");
                            try {
                                answered_by = documentSnapshot.getString("answered_by");
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            question = documentSnapshot.getString("question");
                            setupUI();

                        }else{

                            Toasty.error(getApplicationContext(),"The question has been deleted",Toasty.LENGTH_LONG,true).show();
                            finish();

                        }
                    })
                    .addOnFailureListener(Throwable::printStackTrace);
        }

    }

    private void setupUI(){

        adapter=new AnswersAdapter(answers,author_id, doc_id, "Questions", answered_by);
        mRecyclerView.setAdapter(adapter);

        author_textview=findViewById(R.id.auth_sub);
        question_textview=findViewById(R.id.question);
        answer=findViewById(R.id.answer);

        question_textview.setText(question);

        author_textview.setText(String.format("Asked by %s ( %s )", author, TimeAgo.using(Long.parseLong(timestamp))));
        toolbar.setSubtitle("Asked by " + author + " ( " + TimeAgo.using(Long.parseLong(timestamp)) + " )");
        getSupportActionBar().setSubtitle("Asked by " + author + " ( " + TimeAgo.using(Long.parseLong(timestamp)) + " )");

        /*

        mFirestore.collection("Questions")
                .document(doc_id)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(!TextUtils.isEmpty(documentSnapshot.getString("answered_by"))){
                            answer.setEnabled(false);
                            answer.setHint("Question closed by "+author);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("error",e.getLocalizedMessage());
                    }
                });
        */

        refreshLayout.setOnRefreshListener(this::getAnswers);

        getAnswers();

    }

    private void getAnswers() {

        answers.clear();
        refreshLayout.setRefreshing(true);
        findViewById(R.id.default_item).setVisibility(View.GONE);

        mFirestore.collection("Answers")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .whereEqualTo("question_id",doc_id)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                        if(e!=null){
                            refreshLayout.setRefreshing(false);
                            Log.e(TAG,e.getLocalizedMessage());
                            return;
                        }

                        if(queryDocumentSnapshots.isEmpty()){
                            refreshLayout.setRefreshing(false);
                            findViewById(R.id.default_item).setVisibility(View.VISIBLE);
                        }else{
                            for(DocumentChange doc:queryDocumentSnapshots.getDocumentChanges()) {

                                if (doc.getType() == DocumentChange.Type.ADDED) {

                                    refreshLayout.setRefreshing(false);
                                    Answers answer = doc.getDocument().toObject(Answers.class).withId(doc.getDocument().getId());
                                    if (doc.getDocument().getString("is_answer").toLowerCase().equals("yes")) {
                                        answers.add(0, answer);
                                    } else {
                                        answers.add(answer);
                                    }
                                    adapter.notifyDataSetChanged();


                                }

                            }
                        }

                    }
                });

    }

    public void sendAnswer(View view) {
        if(!TextUtils.isEmpty(answer.getText().toString())) {

            final ProgressDialog mDialog=new ProgressDialog(this);
            mDialog.setMessage("Please wait....");
            mDialog.setIndeterminate(true);
            mDialog.setCancelable(false);
            mDialog.show();

            mFirestore.collection("Users")
                    .document(mCurrentUser.getUid())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(final DocumentSnapshot documentSnapshot) {
                            Map<String,Object> answerMap=new HashMap<>();
                            answerMap.put("user_id",documentSnapshot.getString("id"));
                            answerMap.put("name",documentSnapshot.getString("name"));
                            answerMap.put("timestamp",String.valueOf(System.currentTimeMillis()));
                            answerMap.put("answer",answer.getText().toString());
                            answerMap.put("question_id",doc_id);
                            answerMap.put("is_answer","no");
                            answerMap.put("answered_by", "");
                            answerMap.put("answered_by_id", "");

                            mFirestore.collection("Answers")
                                    .add(answerMap)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {

                                            Map<String, Object> notificationMap = new HashMap<>();
                                            notificationMap.put("answered_user_id",documentSnapshot.getString("id"));
                                            notificationMap.put("timestamp",String.valueOf(System.currentTimeMillis()));
                                            notificationMap.put("question_id",doc_id);

                                            FirebaseFirestore.getInstance()
                                                    .collection("Answered_Notifications")
                                                    .add(notificationMap)
                                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                        @Override
                                                        public void onSuccess(DocumentReference documentReference) {

                                                            UserHelper userHelper=new UserHelper(AnswersActivity.this);
                                                            Cursor rs = userHelper.getData(1);
                                                            rs.moveToFirst();

                                                            String image = rs.getString(rs.getColumnIndex(UserHelper.CONTACTS_COLUMN_IMAGE));
                                                            String username = rs.getString(rs.getColumnIndex(UserHelper.CONTACTS_COLUMN_USERNAME));

                                                            if (!rs.isClosed()) {
                                                                rs.close();
                                                            }

                                                            addToNotification(author_id,mCurrentUser.getUid(),image,username,"Answered to your question",doc_id,"forum",mDialog);


                                                        }
                                                    });


                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            mDialog.dismiss();
                                            Log.e(TAG,e.getLocalizedMessage());
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mDialog.dismiss();
                            Log.e(TAG,e.getLocalizedMessage());
                        }
                    });
        }
    }
}
