package com.nsromapa.frenzapp.newfy.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.nsromapa.frenzapp.newfy.R;
import com.nsromapa.frenzapp.newfy.models.Notification;
import com.nsromapa.frenzapp.newfy.ui.activities.forum.AnswersActivity;
import com.nsromapa.frenzapp.newfy.ui.activities.friends.FriendProfile;
import com.nsromapa.frenzapp.newfy.ui.activities.post.SinglePostView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

/**
 * Created by SAY on 30/8/19.
 */

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {

    private List<Notification> notificationsList;
    private Context context;

    public NotificationsAdapter(List<Notification> notificationsList, Context context) {
        this.notificationsList = notificationsList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification,parent,false);
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

        Notification notification=notificationsList.get(position);

        Glide.with(context)
                .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                .load(notification.getImage())
                .into(holder.image);

        holder.title.setText(notification.getUsername());
        holder.body.setText(notification.getMessage());
        holder.timestamp.setText(TimeAgo.using(Long.parseLong(notification.getTimestamp())));

        if(notification.getType().equals("like")){
            holder.type_image.setImageResource(R.drawable.ic_favorite_red_24dp);
        }else if(notification.getType().equals("comment")){
            holder.type_image.setImageResource(R.drawable.ic_comment_blue);
        }else if(notification.getType().equals("friend_req")){
            holder.type_image.setImageResource(R.drawable.ic_person_add_yellow_24dp);
        }else if(notification.getType().equals("accept_friend_req")){
            holder.type_image.setImageResource(R.drawable.ic_person_green_24dp);
        }else{
            holder.type_image.setImageResource(R.drawable.ic_forum_black_24dp);
        }

        holder.itemView.setOnClickListener(v -> {

            if(notification.getType().equals("like") || notification.getType().equals("comment")){

                context.startActivity(new Intent(context, SinglePostView.class).putExtra("post_id",notification.getAction_id()));

            }else if(notification.getType().equals("friend_req") || notification.getType().equals("accept_friend_req")){

                FriendProfile.startActivity(context,notification.getAction_id());

            }else{

                AnswersActivity.startActivity(context,notification.getAction_id());

            }

        });

        holder.itemView.setOnLongClickListener(v -> {

            new MaterialDialog.Builder(context)
                    .title("Delete notification")
                    .content("Are you sure do you want to delete this notification?")
                    .positiveText("Yes")
                    .negativeText("No")
                    .onPositive((dialog, which) -> {

                        FirebaseFirestore.getInstance()
                                .collection("Users")
                                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .collection("Info_Notifications")
                                .document(notificationsList.get(holder.getAdapterPosition()).documentId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                })
                                .addOnFailureListener(e -> e.printStackTrace());

                        notificationsList.remove(holder.getAdapterPosition());
                        Toasty.success(context,"Notification removed",Toasty.LENGTH_SHORT,true).show();
                        notifyDataSetChanged();

                    })
                    .show();

            return true;
        });

    }

    @Override
    public int getItemCount() {
        return notificationsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private View mView;
        private CircleImageView image;
        private ImageView type_image;
        private TextView title,body,timestamp;

        public ViewHolder(View itemView) {
            super(itemView);

            mView=itemView;
            image = mView.findViewById(R.id.image);
            type_image = mView.findViewById(R.id.type_image);
            title = mView.findViewById(R.id.title);
            body = mView.findViewById(R.id.body);
            timestamp=mView.findViewById(R.id.timestamp);

        }
    }
}
