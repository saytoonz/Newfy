package com.nsromapa.frenzapp.newfy.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.nsromapa.frenzapp.newfy.R;
import com.nsromapa.frenzapp.newfy.models.Comment;
import com.nsromapa.frenzapp.newfy.ui.activities.friends.FriendProfile;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

/**
 * Created by SAY on 30/8/19.
 */

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {

    private List<Comment> commentList;
    private Context context;
    private FirebaseFirestore mFirestore;
    private FirebaseUser mCurrentUser;
    private boolean isOwner;

    public CommentsAdapter(List<Comment> commentList, Context context,boolean owner) {
        this.commentList = commentList;
        this.context = context;
        this.isOwner=owner;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        mFirestore = FirebaseFirestore.getInstance();
        mCurrentUser= FirebaseAuth.getInstance().getCurrentUser();
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
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

       if(isOwner){
           enableDeletion(holder);
       }else{

           if (commentList.get(position).getId().equals(mCurrentUser.getUid())){
              enableDeletion(holder);
           }

       }

        holder.username.setText(commentList.get(position).getUsername());
       holder.username.setOnClickListener(v -> FriendProfile.startActivity(context,commentList.get(holder.getAdapterPosition()).getId()));

       holder.image.setOnClickListener(v -> FriendProfile.startActivity(context,commentList.get(holder.getAdapterPosition()).getId()));

        Glide.with(context)
                .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                .load(commentList.get(position).getImage())
                .into(holder.image);

        holder.comment.setText(commentList.get(position).getComment());

        String timeAgo = TimeAgo.using(Long.parseLong(commentList.get(position).getTimestamp()));
        holder.timestamp.setText(String.format(Locale.ENGLISH,"Commented %s", timeAgo));

        try {
            mFirestore.collection("Users")
                    .document(commentList.get(position).getId())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {

                        try {
                            if (!documentSnapshot.getString("username").equals(commentList.get(holder.getAdapterPosition()).getUsername()) &&
                                    !documentSnapshot.getString("image").equals(commentList.get(holder.getAdapterPosition()).getImage())) {

                                Map<String, Object> commentMap = new HashMap<>();
                                commentMap.put("username", documentSnapshot.getString("username"));
                                commentMap.put("image", documentSnapshot.getString("image"));

                                mFirestore.collection("Posts")
                                        .document(commentList.get(holder.getAdapterPosition()).getPost_id())
                                        .collection("Comments")
                                        .document(commentList.get(holder.getAdapterPosition()).commentId)
                                        .update(commentMap)
                                        .addOnSuccessListener(aVoid -> Log.i("comment_update", "success"))
                                        .addOnFailureListener(e -> Log.i("comment_update", "failure"));

                                holder.username.setText(documentSnapshot.getString("username"));

                                Glide.with(context)
                                        .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                                        .load(documentSnapshot.getString("image"))
                                        .into(holder.image);


                            } else if (!documentSnapshot.getString("username").equals(commentList.get(holder.getAdapterPosition()).getUsername())) {


                                Map<String, Object> commentMap = new HashMap<>();
                                commentMap.put("username", documentSnapshot.getString("username"));

                                mFirestore.collection("Posts")
                                        .document(commentList.get(holder.getAdapterPosition()).getPost_id())
                                        .collection("Comments")
                                        .document(commentList.get(holder.getAdapterPosition()).commentId)
                                        .update(commentMap)
                                        .addOnSuccessListener(aVoid -> Log.i("comment_update", "success"))
                                        .addOnFailureListener(e -> Log.i("comment_update", "failure"));

                                holder.username.setText(documentSnapshot.getString("username"));

                            } else if (!documentSnapshot.getString("image").equals(commentList.get(holder.getAdapterPosition()).getImage())) {

                                Map<String, Object> commentMap = new HashMap<>();
                                commentMap.put("image", documentSnapshot.getString("image"));

                                mFirestore.collection("Posts")
                                        .document(commentList.get(holder.getAdapterPosition()).getPost_id())
                                        .collection("Comments")
                                        .document(commentList.get(holder.getAdapterPosition()).commentId)
                                        .update(commentMap)
                                        .addOnSuccessListener(aVoid -> Log.i("comment_update", "success"))
                                        .addOnFailureListener(e -> Log.i("comment_update", "failure"));

                                Glide.with(context)
                                        .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                                        .load(documentSnapshot.getString("image"))
                                        .into(holder.image);

                            }


                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    })
                    .addOnFailureListener(e -> Log.e("Error", e.getMessage()));
        }catch (Exception ex){
            Log.w("error","fastscrolled",ex);
        }

    }

    private void enableDeletion(final ViewHolder holder) {

        holder.delete.setVisibility(View.VISIBLE);
        holder.delete.setAlpha(0.0f);

        holder.delete.animate()
                .alpha(1.0f)
                .start();

        holder.delete.setOnClickListener(v -> new MaterialDialog.Builder(context)
                .title("Delete comment")
                .content("Are you sure do you want to delete your comment?")
                .positiveText("Yes")
                .negativeText("No")
                .onPositive((dialog, which) -> {
                    dialog.dismiss();

                    final ProgressDialog progressDialog=new ProgressDialog(context);
                    progressDialog.setMessage("Deleting comment...");
                    progressDialog.setIndeterminate(true);
                    progressDialog.show();

                    mFirestore.collection("Posts")
                            .document(commentList.get(holder.getAdapterPosition()).getPost_id())
                            .collection("Comments")
                            .document(commentList.get(holder.getAdapterPosition()).commentId)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                commentList.remove(holder.getAdapterPosition());
                                Toasty.success(context, "Comment deleted", Toasty.LENGTH_SHORT,true).show();
                                notifyDataSetChanged();
                                progressDialog.dismiss();
                            })
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Toasty.error(context, "Error deleting comment: "+e.getLocalizedMessage(), Toasty.LENGTH_SHORT,true).show();
                                Log.w("Error","delete comment",e);
                            });

                })
                .onNegative((dialog, which) -> dialog.dismiss())
                .show());

    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;
        private CircleImageView image;
        private TextView username, comment, timestamp;
        private ImageView delete;

        public ViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            image = mView.findViewById(R.id.comment_user_image);
            username = mView.findViewById(R.id.comment_username);
            comment = mView.findViewById(R.id.comment_text);
            timestamp = mView.findViewById(R.id.comment_timestamp);
            delete=mView.findViewById(R.id.delete);

        }
    }
}
