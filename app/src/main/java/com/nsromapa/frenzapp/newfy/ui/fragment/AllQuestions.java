package com.nsromapa.frenzapp.newfy.ui.fragment;

import android.content.Context;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.nsromapa.frenzapp.newfy.R;
import com.nsromapa.frenzapp.newfy.adapters.QuestionAdapter;
import com.nsromapa.frenzapp.newfy.models.AllQuestionsModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;


import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class AllQuestions extends Fragment {


    private RecyclerView recyclerView;
    private Context context;
    private FirebaseFirestore mFirestore;
    private FirebaseUser mCurrentUser;
    private QuestionAdapter adapter;
    private static String TAG=AllQuestions.class.getSimpleName();
    private List<AllQuestionsModel> allQuestionsModelList =new ArrayList<>();
    private View view;
    private TextView et0,et1,et2,et3,et4,et5,et6,et7,et8,et9,et10,et11,et12,et13;
    private SwipeRefreshLayout refreshLayout;

    public AllQuestions() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.frag_answered, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context = view.getContext();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (mCurrentUser != null) {

            et0=view.findViewById(R.id.all);
            et1=view.findViewById(R.id.accountancy);
            et2=view.findViewById(R.id.astronomy);
            et3=view.findViewById(R.id.biology);
            et4=view.findViewById(R.id.business_maths);
            et5=view.findViewById(R.id.computer_science);
            et6=view.findViewById(R.id.commerce);
            et7=view.findViewById(R.id.chemistry);
            et8=view.findViewById(R.id.economics);
            et9=view.findViewById(R.id.geography);
            et10=view.findViewById(R.id.history);
            et11=view.findViewById(R.id.physics);
            et12=view.findViewById(R.id.p_science);
            et13=view.findViewById(R.id.maths);

            mFirestore = FirebaseFirestore.getInstance();
            adapter = new QuestionAdapter(allQuestionsModelList);
            refreshLayout=view.findViewById(R.id.refreshLayout);

            recyclerView = view.findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
            recyclerView.setItemAnimator(new DefaultItemAnimator());

            allQuestionsModelList.clear();
            recyclerView.setAdapter(adapter);

            refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    allQuestionsModelList.clear();
                    adapter.notifyDataSetChanged();
                    getQuestions();
                }
            });

            setUpOnClick();
            getQuestions();

        }

    }

    private void getQuestions() {

        view.findViewById(R.id.default_item).setVisibility(View.GONE);
        refreshLayout.setRefreshing(true);
        Query firstQuery = mFirestore.collection("Questions")
                .orderBy("timestamp", Query.Direction.DESCENDING);
        firstQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (!documentSnapshots.isEmpty()) {

                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                        if (doc.getType() == DocumentChange.Type.ADDED) {

                            if (!doc.getDocument().getString("id").equals(mCurrentUser.getUid())) {
                                AllQuestionsModel question = doc.getDocument().toObject(AllQuestionsModel.class).withId(doc.getDocument().getId());
                                allQuestionsModelList.add(question);
                                adapter.notifyDataSetChanged();
                                refreshLayout.setRefreshing(false);
                            }

                        }

                    }

                    if(allQuestionsModelList.isEmpty()){
                        view.findViewById(R.id.default_item).setVisibility(View.VISIBLE);
                        refreshLayout.setRefreshing(false);
                    }

                } else {
                    view.findViewById(R.id.default_item).setVisibility(View.VISIBLE);
                    refreshLayout.setRefreshing(false);
                }

            }

        });


    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    public void filterResult(String subject){

        if(subject.equals("All")){
            getQuestions();
        }else{

            refreshLayout.setRefreshing(true);
            view.findViewById(R.id.default_item).setVisibility(View.GONE);

            Query firstQuery = mFirestore.collection("Questions")
                    .whereEqualTo("subject",subject)
                    .orderBy("timestamp", Query.Direction.DESCENDING);
            firstQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                    try {

                        if (!documentSnapshots.isEmpty()) {

                            for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                                if (doc.getType() == DocumentChange.Type.ADDED) {

                                    if (!doc.getDocument().getString("id").equals(mCurrentUser.getUid())) {
                                        AllQuestionsModel question = doc.getDocument().toObject(AllQuestionsModel.class).withId(doc.getDocument().getId());
                                        allQuestionsModelList.add(question);
                                        adapter.notifyDataSetChanged();
                                        refreshLayout.setRefreshing(false);
                                    }

                                }



                            }
                            if(allQuestionsModelList.isEmpty()){
                                view.findViewById(R.id.default_item).setVisibility(View.VISIBLE);
                                refreshLayout.setRefreshing(false);
                            }

                        } else {
                            view.findViewById(R.id.default_item).setVisibility(View.VISIBLE);
                            refreshLayout.setRefreshing(false);
                        }
                    }catch (NullPointerException eee){
                        Toasty.error(context, "Some technical error occurred", Toasty.LENGTH_SHORT,true).show();
                        adapter.notifyDataSetChanged();
                        if(allQuestionsModelList.isEmpty()){
                            view.findViewById(R.id.default_item).setVisibility(View.VISIBLE);
                            refreshLayout.setRefreshing(false);
                        }
                    } catch (Exception ee){
                        ee.printStackTrace();
                        Toasty.error(context, "Some technical error occurred", Toasty.LENGTH_SHORT, true).show();
                        if(allQuestionsModelList.isEmpty()){
                            refreshLayout.setRefreshing(false);
                            view.findViewById(R.id.default_item).setVisibility(View.VISIBLE);
                        }
                    }

                }

            });


        }

    }

    public void setUpOnClick(){

        et0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allQuestionsModelList.clear();
                adapter.notifyDataSetChanged();
                getQuestions();
                refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        allQuestionsModelList.clear();
                        adapter.notifyDataSetChanged();
                        getQuestions();
                    }
                });
            }
        });
        et1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allQuestionsModelList.clear();
                adapter.notifyDataSetChanged();
                filterResult("Accountancy");

                refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        allQuestionsModelList.clear();
                        adapter.notifyDataSetChanged();
                        filterResult("Accountancy");
                    }
                });
            }
        });
        et2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allQuestionsModelList.clear();
                adapter.notifyDataSetChanged();
                filterResult("Astronomy");

                refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        allQuestionsModelList.clear();
                        adapter.notifyDataSetChanged();
                        filterResult("Astronomy");
                    }
                });
            }
        });
        et3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allQuestionsModelList.clear();
                adapter.notifyDataSetChanged();
                filterResult("Biology");

                refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        allQuestionsModelList.clear();
                        adapter.notifyDataSetChanged();
                        filterResult("Biology");
                    }
                });
            }
        });
        et4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allQuestionsModelList.clear();
                adapter.notifyDataSetChanged();
                filterResult("Business Maths");

                refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        allQuestionsModelList.clear();
                        adapter.notifyDataSetChanged();
                        filterResult("Business Maths");
                    }
                });
            }
        });
        et5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allQuestionsModelList.clear();
                adapter.notifyDataSetChanged();
                filterResult("Computer Science");

                refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        allQuestionsModelList.clear();
                        adapter.notifyDataSetChanged();
                        filterResult("Computer Science");
                    }
                });
            }
        });
        et6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allQuestionsModelList.clear();
                adapter.notifyDataSetChanged();
                filterResult("Commerce");

                refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        allQuestionsModelList.clear();
                        adapter.notifyDataSetChanged();
                        filterResult("Commerce");
                    }
                });
            }
        });
        et7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allQuestionsModelList.clear();
                adapter.notifyDataSetChanged();
                filterResult("Chemistry");

                refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        allQuestionsModelList.clear();
                        adapter.notifyDataSetChanged();
                        filterResult("Chemistry");
                    }
                });
            }
        });
        et8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allQuestionsModelList.clear();
                adapter.notifyDataSetChanged();
                filterResult("Economics");

                refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        allQuestionsModelList.clear();
                        adapter.notifyDataSetChanged();
                        filterResult("Economics");
                    }
                });
            }
        });
        et9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allQuestionsModelList.clear();
                adapter.notifyDataSetChanged();
                filterResult("Geography");

                refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        allQuestionsModelList.clear();
                        adapter.notifyDataSetChanged();
                        filterResult("Geography");
                    }
                });
            }
        });
        et10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allQuestionsModelList.clear();
                adapter.notifyDataSetChanged();
                filterResult("History");

                refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        allQuestionsModelList.clear();
                        adapter.notifyDataSetChanged();
                        filterResult("History");
                    }
                });
            }
        });
        et11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allQuestionsModelList.clear();
                adapter.notifyDataSetChanged();
                filterResult("Physics");

                refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        allQuestionsModelList.clear();
                        adapter.notifyDataSetChanged();
                        filterResult("Physics");
                    }
                });
            }
        });
        et12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allQuestionsModelList.clear();
                adapter.notifyDataSetChanged();
                filterResult("Political Science");

                refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        allQuestionsModelList.clear();
                        adapter.notifyDataSetChanged();
                        filterResult("Political Science");
                    }
                });
            }
        });
        et13.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allQuestionsModelList.clear();
                adapter.notifyDataSetChanged();
                filterResult("Maths");

                refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        allQuestionsModelList.clear();
                        adapter.notifyDataSetChanged();
                        filterResult("Maths");
                    }
                });
            }
        });

    }

}
