package com.nsromapa.frenzapp.newfy.ui.fragment;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.nsromapa.frenzapp.newfy.R;
import com.nsromapa.frenzapp.newfy.newsapi.Adapter;
import com.nsromapa.frenzapp.newfy.newsapi.Utils;
import com.nsromapa.frenzapp.newfy.newsapi.api.ApiClient;
import com.nsromapa.frenzapp.newfy.newsapi.api.ApiInterface;
import com.nsromapa.frenzapp.newfy.newsapi.models.Article;
import com.nsromapa.frenzapp.newfy.newsapi.models.News;
import com.nsromapa.frenzapp.newfy.utils.Config;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Discover extends Fragment implements SwipeRefreshLayout.OnRefreshListener {


    private RecyclerView recyclerView;
    private Context context;
    private SwipeRefreshLayout refreshLayout;
    private View view;
    private FirebaseAuth mAuth;
    private LinearLayout default_item;
    private List<Article> articles = new ArrayList<>();
    private Adapter adapter;
    private ImageView default_image;
    private TextView default_title,default_text;
    private Button retryBtn;

    private TextView t1,t2,t3,t4,t5,t6,t7;
    private String category;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_discover, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context = view.getContext();
        refreshLayout=view.findViewById(R.id.refreshLayout);

        mAuth=FirebaseAuth.getInstance();
        default_item=view.findViewById(R.id.default_item);
        default_image=view.findViewById(R.id.default_image);
        default_text=view.findViewById(R.id.default_text);
        default_title=view.findViewById(R.id.default_title);
        retryBtn=view.findViewById(R.id.retry_btn);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        t1=view.findViewById(R.id.t1);
        t2=view.findViewById(R.id.t2);
        t3=view.findViewById(R.id.t3);
        t4=view.findViewById(R.id.t4);
        t5=view.findViewById(R.id.t5);
        t6=view.findViewById(R.id.t6);
        t7=view.findViewById(R.id.t7);

        category="general";
        refreshLayout.setOnRefreshListener(this);
        loadJSON("","");

        t1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!category.equals("general")) {
                    loadJSON("","");
                    category = "general";
                }
            }
        });

        t2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!category.equals("business")) {
                    category = "business";
                    loadJSON("",category);
                }
            }
        });

        t3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!category.equals("entertainment")) {
                    category = "entertainment";
                    loadJSON("",category);
                }
            }
        });

        t4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!category.equals("health")) {
                    category = "health";
                    loadJSON("",category);
                }
            }
        });

        t5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!category.equals("science")) {
                    category = "science";
                    loadJSON("",category);
                }
            }
        });

        t6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!category.equals("sports")) {
                    category = "sports";
                    loadJSON("",category);
                }
            }
        });

        t7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!category.equals("technology")) {
                    category = "technology";
                    loadJSON("",category);
                }
            }
        });

    }

    @Override
    public void onRefresh() {
        refreshLayout.setRefreshing(false);
        //if(category.equals("general")) {
            //loadJSON("", "");
            //refreshLayout.setRefreshing(false);
        //}else{
            //if(category.length()>0){
                //loadJSON("",category);
               // refreshLayout.setRefreshing(false);
            //}
        //}
    }

    private void loadJSON(final String keyword,final String category){

        default_item.setVisibility(View.GONE);
        refreshLayout.setRefreshing(true);

        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);

        String country = Utils.getCountry();
        String language = Utils.getLanguage();

        Call<News> call;

        if (keyword.length() > 0 ){
            call = apiInterface.getNewsSearch(keyword, language, "publishedAt", Config.NEWS_API_KEY);
        } else {

            if(category.length()>0) {
                call = apiInterface.getNewsByCategory(country,category, Config.NEWS_API_KEY);
            }else{
                call = apiInterface.getNews(country, Config.NEWS_API_KEY);
            }
        }

        call.enqueue(new Callback<News>() {
            @Override
            public void onResponse(Call<News> call, Response<News> response) {
                if (response.isSuccessful() && response.body().getArticle() != null){

                    if (!articles.isEmpty()){
                        articles.clear();
                    }

                    Log.i("NewsApi",response.toString());

                    articles = response.body().getArticle();
                    adapter = new Adapter(articles, context);
                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();

                    initListener();

                    //topHeadline.setVisibility(View.VISIBLE);
                    refreshLayout.setRefreshing(false);


                } else {

                    //topHeadline.setVisibility(View.INVISIBLE);
                    refreshLayout.setRefreshing(false);

                    String errorCode;
                    switch (response.code()) {
                        case 404:
                            errorCode = "404 not found";
                            break;
                        case 500:
                            errorCode = "500 server broken";
                            break;
                        default:
                            errorCode = "unknown error";
                            break;
                    }

                    showErrorMessage(
                            R.drawable.no_result,
                            "No Result",
                            "Please try again!\n"+
                                    errorCode);

                }
            }

            @Override
            public void onFailure(Call<News> call, Throwable t) {
                //topHeadline.setVisibility(View.INVISIBLE);
                refreshLayout.setRefreshing(false);
                showErrorMessage(
                        R.drawable.oops,
                        "Oops..",
                        "You are not connected to the internet");
            }
        });

    }

    private void showErrorMessage(int imageView, String title, String message){

        if (default_item.getVisibility() == View.GONE) {
            default_item.setVisibility(View.VISIBLE);
        }

        default_image.setImageResource(imageView);
        default_title.setText(title);
        default_text.setText(message);

        retryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadJSON("","");
            }
        });

    }


    private void initListener(){

        adapter.setOnItemClickListener(new Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                ImageView imageView = view.findViewById(R.id.img);
                /*Intent intent = new Intent(context, MainActivity.class);

                intent.putExtra("url", article.getUrl());
                intent.putExtra("title", article.getTitle());
                intent.putExtra("img",  article.getUrlToImage());
                intent.putExtra("date",  article.getPublishedAt());
                intent.putExtra("source",  article.getSource().getName());
                intent.putExtra("author",  article.getAuthor());*/

                Article article = articles.get(position);
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(article.getUrl()));
                startActivity(i);

            }
        });

    }


}

