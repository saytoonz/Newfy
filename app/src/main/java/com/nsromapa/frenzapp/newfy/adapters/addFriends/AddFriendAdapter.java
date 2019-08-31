package com.nsromapa.frenzapp.newfy.adapters.addFriends;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nsromapa.frenzapp.newfy.R;
import com.nsromapa.frenzapp.newfy.models.Friends;
import com.nsromapa.frenzapp.newfy.ui.activities.friends.FriendProfile;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.marcoscg.dialogsheet.DialogSheet;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by SAY on 30/8/19.
 */

public class AddFriendAdapter extends RecyclerView.Adapter<AddFriendAdapter.ViewHolder> {

    private List<Friends> usersList;
    private Context context;
    private View view;
    private HashMap<String, Object> userMap;
    private ViewHolder holderr;

    public AddFriendAdapter(List<Friends> usersList, Context context, View view) {
        this.usersList = usersList;
        this.context = context;
        this.view = view;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.item_friend,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        holderr=holder;
        checkIfReqSent(holder);
        holder.name.setText(usersList.get(position).getName());
        if(holder.username.getText().equals("null")){
            holder.username.setText("loading...");
        }

        holder.username.setText("@"+usersList.get(position).getUsername());

        holder.listenerText.setText("Add as friend");

        Glide.with(context)
                .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                .load(usersList.get(position).getImage())
                .into(holder.image);

        holder.mView.setOnClickListener(view -> FriendProfile.startActivity(context,usersList.get(holder.getAdapterPosition()).userId));

        holder.exist_icon.setOnClickListener(view -> {
            final AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setTitle("Information");
            dialog.setMessage("This icons shows to indicate that friend request to this user has been sent already.");
            dialog.setPositiveButton("Ok", (dialogInterface, i) -> dialogInterface.dismiss()).setIcon(R.drawable.ic_call_made_black_24dp).show();
        });


        holder.friend_icon.setOnClickListener(view -> {
            final AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setTitle("Information");
            dialog.setMessage("This icons shows to indicate that the user is already your friend.");
            dialog.setPositiveButton("Ok", (dialogInterface, i) -> dialogInterface.dismiss()).setIcon(R.drawable.ic_friend).show();
        });
    }

    private void checkIfReqSent(final ViewHolder holder) {

        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(usersList.get(holder.getAdapterPosition()).userId)
                .collection("Friends")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        holder.progressBar.setVisibility(View.GONE);
                        holder.exist_icon.setVisibility(View.GONE);
                        holder.friend_icon.setVisibility(View.VISIBLE);
                        holder.friend_icon.setAlpha(0.0f);

                        holder.friend_icon.animate()
                                .setDuration(200)
                                .alpha(1.0f)
                                .start();
                    } else {
                        try {
                            FirebaseFirestore.getInstance()
                                    .collection("Users")
                                    .document(usersList.get(holder.getAdapterPosition()).userId)
                                    .collection("Friend_Requests")
                                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .get().addOnSuccessListener(documentSnapshot1 -> {
                                        if (documentSnapshot1.exists()) {
                                            holder.progressBar.setVisibility(View.GONE);
                                            holder.friend_icon.setVisibility(View.GONE);
                                            holder.exist_icon.setVisibility(View.VISIBLE);
                                            holder.exist_icon.setAlpha(0.0f);

                                            holder.exist_icon.animate()
                                                    .alpha(1.0f)
                                                    .start();
                                        } else {
                                            holder.progressBar.setVisibility(View.GONE);
                                            holder.exist_icon.setVisibility(View.GONE);
                                            holder.friend_icon.setVisibility(View.GONE);
                                        }
                                    });
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                });

    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public void removeItem(final int position, final Snackbar snackbar, final int deletedIndex, final Friends deletedItem) {

        new DialogSheet(context)
                .setTitle("Add Friend")
                .setMessage("Are you sure do you want to add " + usersList.get(position).getName() + " to your friend list?")
                .setPositiveButton("Yes", v -> addUser(position, deletedIndex, deletedItem))
                .setNegativeButton("No", v -> notifyDataSetChanged())
                .setRoundedCorners(true)
                .setColoredNavigationBar(true)
                .show();


    }

    private void addUser( final int position, final int deletedIndex, final Friends deletedItem) {

        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(usersList.get(position).userId)
                .collection("Friends")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (!documentSnapshot.exists()) {

                        FirebaseFirestore.getInstance()
                                .collection("Users")
                                .document(usersList.get(position).userId)
                                .collection("Friend_Requests")
                                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .get()
                                .addOnSuccessListener(documentSnapshot1 -> {

                                    if(holderr.friend_icon.getVisibility()!=View.VISIBLE) {

                                        if (!documentSnapshot1.exists()) {
                                            executeFriendReq(deletedItem,holderr);
                                        } else {
                                            Snackbar.make(view, "Friend request has been sent already", Snackbar.LENGTH_LONG).show();
                                            notifyDataSetChanged();
                                        }

                                    }else{
                                        Snackbar.make(view, usersList.get(position).getName()+" is already your friend", Snackbar.LENGTH_LONG).show();
                                        notifyDataSetChanged();
                                    }

                                });

                    } else {
                        usersList.remove(position);
                        notifyDataSetChanged();
                    }

                });


    }

    private void executeFriendReq(final Friends deletedItem, final ViewHolder holder) {

        userMap = new HashMap<>();

        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    final String email=documentSnapshot.getString("email");

                    userMap.put("name",  documentSnapshot.getString("name"));
                    userMap.put("id",    documentSnapshot.getString("id"));
                    userMap.put("email", email);
                    userMap.put("image", documentSnapshot.getString("image"));
                    userMap.put("tokens", documentSnapshot.get("token_ids"));
                    userMap.put("notification_id", String.valueOf(System.currentTimeMillis()));
                    userMap.put("timestamp", String.valueOf(System.currentTimeMillis()));

                    //Add to user
                    FirebaseFirestore.getInstance()
                            .collection("Users")
                            .document(deletedItem.userId)
                            .collection("Friend_Requests")
                            .document(documentSnapshot.getString("id"))
                            .set(userMap)
                            .addOnSuccessListener(aVoid -> {

                                //Add for notification data
                                FirebaseFirestore.getInstance()
                                        .collection("Notifications")
                                        .document(deletedItem.userId)
                                        .collection("Friend_Requests")
                                        .document(email)
                                        .set(userMap)
                                        .addOnSuccessListener(aVoid1 -> {

                                            holder.progressBar.setVisibility(View.GONE);
                                            holder.friend_icon.setVisibility(View.GONE);
                                            holder.exist_icon.setVisibility(View.VISIBLE);
                                            holder.exist_icon.setAlpha(0.0f);

                                            holder.exist_icon.animate()
                                                    .setDuration(200)
                                                    .alpha(1.0f)
                                                    .start();
                                            Snackbar.make(view, "Friend request sent to " + deletedItem.getName(), Snackbar.LENGTH_LONG).show();
                                            notifyDataSetChanged();
                                            notifyItemChanged(holder.getAdapterPosition());

                                        }).addOnFailureListener(e -> {
                                            holder.progressBar.setVisibility(View.GONE);
                                            Log.e("Error",e.getMessage());
                                        });


                            }).addOnFailureListener(e -> {
                                holder.progressBar.setVisibility(View.GONE);
                                Log.e("Error",e.getMessage());
                            });

                });

    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        CircleImageView image;
        View            mView;
        TextView        name, listenerText,username;
        RelativeLayout  viewBackground, viewForeground;
        ImageView       exist_icon,friend_icon;
        ProgressBar     progressBar;

        ViewHolder(View itemView) {
            super(itemView);

            mView =itemView;
            progressBar=mView.findViewById(R.id.progressBar);
            image = mView.findViewById(R.id.image);
            name = mView.findViewById(R.id.name);
            username = mView.findViewById(R.id.username);
            viewBackground = mView.findViewById(R.id.view_background);
            viewForeground = mView.findViewById(R.id.view_foreground);
            listenerText = mView.findViewById(R.id.view_foreground_text);
            exist_icon   = mView.findViewById(R.id.exist_icon);
            friend_icon  = mView.findViewById(R.id.friend_icon);


        }
    }
}
