package com.nsromapa.frenzapp.newfy.adapters.viewFriends;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;


import androidx.recyclerview.widget.RecyclerView;

import com.nsromapa.frenzapp.newfy.R;
import com.nsromapa.frenzapp.newfy.models.ViewFriends;
import com.nsromapa.frenzapp.newfy.ui.activities.friends.FriendProfile;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.marcoscg.dialogsheet.DialogSheet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by SAY on 30/8/19.
 */

public class ViewFriendAdapter extends RecyclerView.Adapter<ViewFriendAdapter.ViewHolder> {

    private List<ViewFriends> usersList;
    private Context context;
    private HashMap<String, Object> userMap;
    private ProgressDialog mDialog;

    public ViewFriendAdapter(List<ViewFriends> usersList, Context context) {
        this.usersList = usersList;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_added,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {


        holder.name.setText(usersList.get(position).getName());

        if(holder.username.getText().equals("null")){
            holder.username.setText("loading...");
        }

        holder.username.setText("@"+usersList.get(position).getUsername());

        Glide.with(context)
                .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                .load(usersList.get(position).getImage())
                .into(holder.image);

        try {

            FirebaseFirestore.getInstance().collection("Users")
                    .document(usersList.get(position).getId())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {

                        try {
                            if (!documentSnapshot.getString("name").equals(usersList.get(holder.getAdapterPosition()).getName()) &&
                                    !documentSnapshot.getString("username").equals(usersList.get(holder.getAdapterPosition()).getUsername()) &&
                                    !documentSnapshot.getString("image").equals(usersList.get(holder.getAdapterPosition()).getImage())) {

                                Map<String, Object> user = new HashMap<>();
                                user.put("name", documentSnapshot.getString("name"));
                                user.put("username", documentSnapshot.getString("username"));
                                user.put("image", documentSnapshot.getString("image"));

                                FirebaseFirestore.getInstance().collection("Users")
                                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .collection("Friends")
                                        .document(usersList.get(holder.getAdapterPosition()).getId())
                                        .update(user)
                                        .addOnSuccessListener(aVoid -> Log.i("friend_update", "success"))
                                        .addOnFailureListener(e -> Log.i("friend_update", "failure"));

                                holder.name.setText(documentSnapshot.getString("name"));
                                holder.username.setText("@"+documentSnapshot.getString("username"));

                                Glide.with(context)
                                        .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                                        .load(documentSnapshot.getString("image"))
                                        .into(holder.image);


                            }else if (!documentSnapshot.getString("name").equals(usersList.get(holder.getAdapterPosition()).getName()) &&
                                    !documentSnapshot.getString("username").equals(usersList.get(holder.getAdapterPosition()).getUsername())) {

                                Map<String, Object> user = new HashMap<>();
                                user.put("name", documentSnapshot.getString("name"));
                                user.put("username", documentSnapshot.getString("username"));

                                FirebaseFirestore.getInstance().collection("Users")
                                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .collection("Friends")
                                        .document(usersList.get(holder.getAdapterPosition()).getId())
                                        .update(user)
                                        .addOnSuccessListener(aVoid -> Log.i("friend_update", "success"))
                                        .addOnFailureListener(e -> Log.i("friend_update", "failure"));

                                holder.name.setText(documentSnapshot.getString("name"));
                                holder.username.setText("@"+documentSnapshot.getString("username"));


                            }else if (!documentSnapshot.getString("name").equals(usersList.get(holder.getAdapterPosition()).getName()) &&
                                    !documentSnapshot.getString("image").equals(usersList.get(holder.getAdapterPosition()).getImage())) {

                                Map<String, Object> user = new HashMap<>();
                                user.put("name", documentSnapshot.getString("name"));
                                user.put("image", documentSnapshot.getString("image"));

                                FirebaseFirestore.getInstance().collection("Users")
                                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .collection("Friends")
                                        .document(usersList.get(holder.getAdapterPosition()).getId())
                                        .update(user)
                                        .addOnSuccessListener(aVoid -> Log.i("friend_update", "success"))
                                        .addOnFailureListener(e -> Log.i("friend_update", "failure"));

                                holder.name.setText(documentSnapshot.getString("name"));

                                Glide.with(context)
                                        .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                                        .load(documentSnapshot.getString("image"))
                                        .into(holder.image);


                            }else if (!documentSnapshot.getString("username").equals(usersList.get(holder.getAdapterPosition()).getUsername()) &&
                                    !documentSnapshot.getString("image").equals(usersList.get(holder.getAdapterPosition()).getImage())) {

                                Map<String, Object> user = new HashMap<>();
                                user.put("username", documentSnapshot.getString("username"));
                                user.put("image", documentSnapshot.getString("image"));

                                FirebaseFirestore.getInstance().collection("Users")
                                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .collection("Friends")
                                        .document(usersList.get(holder.getAdapterPosition()).getId())
                                        .update(user)
                                        .addOnSuccessListener(aVoid -> Log.i("f" +
                                                "riend_update", "success"))
                                        .addOnFailureListener(e -> Log.i("friend_update", "failure"));

                                holder.username.setText("@"+documentSnapshot.getString("username"));

                                Glide.with(context)
                                        .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                                        .load(documentSnapshot.getString("image"))
                                        .into(holder.image);


                            } else if (!documentSnapshot.getString("name").equals(usersList.get(holder.getAdapterPosition()).getName())) {

                                Map<String, Object> user = new HashMap<>();
                                user.put("name", documentSnapshot.getString("name"));

                                FirebaseFirestore.getInstance().collection("Users")
                                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .collection("Friends")
                                        .document(usersList.get(holder.getAdapterPosition()).getId())
                                        .update(user)
                                        .addOnSuccessListener(aVoid -> Log.i("friend_update", "success"))
                                        .addOnFailureListener(e -> Log.i("friend_update", "failure"));


                                holder.name.setText(documentSnapshot.getString("name"));

                            } else if (!documentSnapshot.getString("image").equals(usersList.get(holder.getAdapterPosition()).getImage())) {

                                Map<String, Object> user = new HashMap<>();
                                user.put("image", documentSnapshot.getString("image"));

                                FirebaseFirestore.getInstance().collection("Users")
                                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .collection("Friends")
                                        .document(usersList.get(holder.getAdapterPosition()).getId())
                                        .update(user)
                                        .addOnSuccessListener(aVoid -> Log.i("friend_update", "success"))
                                        .addOnFailureListener(e -> Log.i("friend_update", "failure"));


                                Glide.with(context)
                                        .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                                        .load(documentSnapshot.getString("image"))
                                        .into(holder.image);

                            }else if (!documentSnapshot.getString("username").equals(usersList.get(holder.getAdapterPosition()).getName())) {

                                Map<String, Object> user = new HashMap<>();
                                user.put("username", documentSnapshot.getString("username"));

                                FirebaseFirestore.getInstance().collection("Users")
                                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .collection("Friends")
                                        .document(usersList.get(holder.getAdapterPosition()).getId())
                                        .update(user)
                                        .addOnSuccessListener(aVoid -> Log.i("friend_update", "success"))
                                        .addOnFailureListener(e -> Log.i("friend_update", "failure"));


                                holder.username.setText("@"+documentSnapshot.getString("username"));

                            }
                        }catch (Exception ex){
                            ex.printStackTrace();
                        }
                    });

        }catch (Exception ex){
            Log.w("error","fastscrolled",ex);
        }

        holder.mView.setOnClickListener(view -> FriendProfile.startActivity(context, usersList.get(holder.getAdapterPosition()).getId()));

    }


    public void removeItem(final int position) {

        new DialogSheet(context)
                .setTitle("Unfriend " + usersList.get(position).getName())
                .setMessage("Are you sure do you want to remove " + usersList.get(position).getName() + " from your friend list?")
                .setPositiveButton("Yes", v -> removeUser(position))
                .setNegativeButton("No", v -> notifyDataSetChanged())
                .setRoundedCorners(true)
                .setColoredNavigationBar(true)
                .show();

    }


    private void removeUser(final int position) {

        FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("Friends").document(usersList.get(position).getId()).delete().addOnSuccessListener(aVoid -> FirebaseFirestore.getInstance()
                        .collection("Users")
                        .document(usersList.get(position).getId())
                        .collection("Friends")
                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .delete()
                        .addOnSuccessListener(aVoid1 -> {
                            usersList.remove(position);
                            notifyItemRemoved(position);
                            notifyDataSetChanged();
                        })).addOnFailureListener(e -> e.printStackTrace());

    }


    @Override
    public int getItemCount() {
        return usersList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView name, listenerText,username;
        RelativeLayout viewBackground, viewForeground;
        View mView;
        CircleImageView image;

        public ViewHolder(View itemView) {
            super(itemView);

            mView=itemView;
            image=mView.findViewById(R.id.image);
            username=mView.findViewById(R.id.username);
            name=mView.findViewById(R.id.name);
            viewBackground =mView.findViewById(R.id.view_background);
            viewForeground =mView.findViewById(R.id.view_foreground);
            listenerText =mView.findViewById(R.id.view_foreground_text);

        }
    }
}
