package com.nsromapa.frenzapp.newfy.ui.fragment;

import android.app.Fragment;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nsromapa.frenzapp.newfy.R;
import com.nsromapa.frenzapp.newfy.ui.activities.friends.SearchUsersActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Created by SAY on 30/8/19.
 */

public class FriendsFragment extends Fragment implements BottomNavigationView.OnNavigationItemReselectedListener,
BottomNavigationView.OnNavigationItemSelectedListener{

    View mView;
    FloatingActionButton fab;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.frag_friends, container, false);
        return mView;
    }

    public static FriendsFragment newInstance(String frag){

        Bundle args=new Bundle();
        args.putString("frag",frag);

        FriendsFragment friendsFragment=new FriendsFragment();
        friendsFragment.setArguments(args);

        return friendsFragment;

    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

       fab=mView.findViewById(R.id.searchFab);
       fab.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               gotoSearch();
           }
       });

        BottomNavigationView bottomNavigationView=mView.findViewById(R.id.bottom_nav);
        if(getArguments()!=null){
            bottomNavigationView.setSelectedItemId(R.id.action_view_request);
           loadFragment(new FriendRequests());
       }else {
           loadFragment(new Friends());
       }
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setOnNavigationItemReselectedListener(this);


    }

    private void loadFragment(Fragment fragment) {
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.container_2, fragment)
                .commit();
    }

    public void gotoSearch() {
        SearchUsersActivity.startActivity(getActivity(), mView.getContext(), fab);
    }

    @Override
    public void onNavigationItemReselected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_view:
                break;
            case R.id.action_view_request:
                break;
            case R.id.action_add:
                break;


        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_view:
                loadFragment(new Friends());
                break;
            case R.id.action_view_request:
                loadFragment(new FriendRequests());
                break;
            case R.id.action_add:
                loadFragment(new com.nsromapa.frenzapp.newfy.ui.fragment.AddFriends());
                break;

        }
        return true;
    }
}
