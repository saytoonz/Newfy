package com.nsromapa.frenzapp.newfy.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.nsromapa.frenzapp.newfy.R;
import com.nsromapa.frenzapp.newfy.models.Answers;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

public class AnswersAdapter extends RecyclerView.Adapter<AnswersAdapter.ViewHolder> {

    private List<Answers> answereds;
    private Context context;
    private String doc_id,type,owner_id,answered_by;

    public AnswersAdapter(List<Answers> unanswereds, String owner_id, String doc_id, String type, String answered_by) {
        this.answereds = unanswereds;
        this.doc_id = doc_id;
        this.type = type;
        this.owner_id = owner_id;
        this.answered_by = answered_by;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context=parent.getContext();
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_answer,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        final Answers answer=answereds.get(holder.getAdapterPosition());
        holder.answer.setText(answer.getAnswer());
        holder.timestamp.setText(TimeAgo.using(Long.parseLong(answer.getTimestamp())));
        holder.name.setText(answer.getName());

        FirebaseFirestore.getInstance().collection("Users")
                .document(answer.getUser_id())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if(!documentSnapshot.getString("name").equals(answer.getName())){

                        Map<String, Object> map = new HashMap<>();
                        map.put("name", documentSnapshot.getString("name"));

                        FirebaseFirestore.getInstance().collection("Answers")
                                .document(answer.Answers_doc_id)
                                .update(map)
                                .addOnSuccessListener(aVoid -> holder.name.setText(documentSnapshot.getString("name")));

                    }

                });

        FirebaseFirestore.getInstance().collection("Users")
                .document(answer.getUser_id())
                .get()
                .addOnSuccessListener(documentSnapshot -> Glide.with(context)
                        .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                        .load(documentSnapshot.getString("image"))
                        .into(holder.profile_pic))
                .addOnFailureListener(e -> e.printStackTrace());

        FirebaseFirestore.getInstance().collection("Users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if(answer.getName().equals(documentSnapshot.getString("name"))){
                        holder.name.setText("You");
                        if(answer.getIs_answer().equals("yes")) {
                            holder.delete.setVisibility(View.GONE);
                        }else{
                            holder.delete.setVisibility(View.VISIBLE);
                        }
                    }
                });

        if(owner_id.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
            holder.unmrk_ans.setVisibility(View.GONE);
            holder.mrk_ans.setVisibility(View.VISIBLE);
        }else{
            holder.mrk_ans.setVisibility(View.GONE);
            holder.unmrk_ans.setVisibility(View.GONE);
        }

        if(answer.getIs_answer().equals("yes")){
            holder.bottom.setBackgroundColor(context.getResources().getColor(R.color.green_bottom));
            if(owner_id.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                holder.mrk_ans.setVisibility(View.GONE);
                holder.unmrk_ans.setVisibility(View.VISIBLE);
            }else{
                holder.mrk_ans.setVisibility(View.VISIBLE);
                holder.mrk_ans.setEnabled(false);
                holder.unmrk_ans.setVisibility(View.GONE);
                holder.mrk_ans.setText("Marked as Answer");
            }
        }else{
            if(TextUtils.isEmpty(answered_by)) {
                if(owner_id.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    holder.unmrk_ans.setVisibility(View.GONE);
                    holder.mrk_ans.setVisibility(View.VISIBLE);
                }else{
                    holder.unmrk_ans.setVisibility(View.GONE);
                    holder.mrk_ans.setVisibility(View.GONE);
                }
            }else{
                holder.unmrk_ans.setVisibility(View.GONE);
                holder.mrk_ans.setVisibility(View.GONE);
            }
        }

        holder.mrk_ans.setOnClickListener(v -> new MaterialDialog.Builder(context)
                .title("Mark as Answer")
                .content("Are you sure do you want to mark it as answer?")
                .positiveText("Yes")
                .negativeText("No")
                .onPositive((dialog, which) -> {

                    dialog.dismiss();
                    final ProgressDialog mDialog=new ProgressDialog(context);
                    mDialog.setMessage("Marking as answer....");
                    mDialog.setIndeterminate(true);
                    mDialog.setCancelable(false);
                    mDialog.setCanceledOnTouchOutside(false);
                    mDialog.show();

                    FirebaseFirestore.getInstance().collection("Questions")
                            .document(doc_id)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {

                                try {
                                    if (TextUtils.isEmpty(documentSnapshot.getString("answered_by"))) {

                                        Map<String, Object> map1 = new HashMap<>();
                                        map1.put("is_answer", "yes");

                                        FirebaseFirestore.getInstance()
                                               .collection("Answers")
                                                .document(answer.Answers_doc_id)
                                                .update(map1)
                                                .addOnSuccessListener(aVoid -> {

                                                    Map<String, Object> map2 = new HashMap<>();
                                                    map2.put("answered_by", answer.getName());
                                                    map2.put("answered_by_id", answer.getUser_id());

                                                    FirebaseFirestore.getInstance()
                                                            .collection("Questions")
                                                            .document(answer.getQuestion_id())
                                                            .update(map2)
                                                            .addOnSuccessListener(aVoid1 -> {

                                                                answered_by=answer.getName();

                                                                Map<String, Object> notificationMap = new HashMap<>();
                                                                notificationMap.put("answered_user_id",answer.getUser_id());
                                                                notificationMap.put("timestamp",String.valueOf(System.currentTimeMillis()));
                                                                notificationMap.put("question_id",answer.getQuestion_id());

                                                                FirebaseFirestore.getInstance()
                                                                        .collection("Marked_Notifications")
                                                                        .add(notificationMap)
                                                                        .addOnSuccessListener(documentReference -> {

                                                                            holder.bottom.setBackgroundColor(context.getResources().getColor(R.color.green_bottom));
                                                                            holder.mrk_ans.setVisibility(View.GONE);
                                                                            holder.unmrk_ans.setVisibility(View.VISIBLE);
                                                                            mDialog.dismiss();
                                                                            Toasty.success(context, "Marked as answer", Toasty.LENGTH_SHORT,true).show();

                                                                        });

                                                            })
                                                            .addOnFailureListener(e -> {
                                                                mDialog.dismiss();
                                                                Log.e("Error", e.getLocalizedMessage());
                                                                Toasty.error(context, "Error marking as answer: " + e.getLocalizedMessage(), Toasty.LENGTH_SHORT,true).show();
                                                            });

                                                })
                                                .addOnFailureListener(e -> {
                                                    mDialog.dismiss();
                                                    Log.e("Error", e.getLocalizedMessage());
                                                    Toasty.error(context, "Error marking as answer: " + e.getLocalizedMessage(), Toasty.LENGTH_SHORT,true).show();
                                                });


                                    } else {

                                        mDialog.dismiss();
                                        Toasty.info(context, "Cannot mark more than one answer as correct.", Toasty.LENGTH_SHORT,true).show();

                                    }
                                }catch (Exception e){
                                    mDialog.dismiss();
                                    e.printStackTrace();
                                }

                            })
                            .addOnFailureListener(e -> {
                                mDialog.dismiss();
                                Log.e("Error",e.getLocalizedMessage());
                                Toasty.error(context, "Error marking as answer: "+e.getLocalizedMessage(), Toasty.LENGTH_SHORT,true).show();
                            });

                })
                .onNegative((dialog, which) -> dialog.dismiss()).show());

        holder.unmrk_ans.setOnClickListener(v -> new MaterialDialog.Builder(context)
                .title("Unmark as Answer")
                .content("Are you sure do you want to unmark it as answer?")
                .positiveText("Yes")
                .negativeText("No")
                .onPositive((dialog, which) -> {

                    dialog.dismiss();
                    final ProgressDialog mDialog=new ProgressDialog(context);
                    mDialog.setMessage("Unmarking as answer....");
                    mDialog.setIndeterminate(true);
                    mDialog.setCancelable(false);
                    mDialog.setCanceledOnTouchOutside(false);
                    mDialog.show();

                    Map<String,Object> map1=new HashMap<>();
                    map1.put("is_answer","no");

                    FirebaseFirestore.getInstance()
                            .collection("Answers")
                            .document(answer.Answers_doc_id)
                            .update(map1)
                            .addOnSuccessListener(aVoid -> {

                                Map<String,Object> map2=new HashMap<>();
                                map2.put("answered_by","");
                                map2.put("answered_by_id","");

                                FirebaseFirestore.getInstance()
                                        .collection("Questions")
                                        .document(answer.getQuestion_id())
                                        .update(map2)
                                        .addOnSuccessListener(aVoid12 -> {

                                            answered_by="";
                                            mDialog.dismiss();
                                            notifyItemChanged(holder.getAdapterPosition());
                                            Toasty.success(context, "Unmarked as answer", Toasty.LENGTH_SHORT,true).show();
                                            notifyDataSetChanged();

                                            holder.bottom.setBackgroundColor(context.getResources().getColor(R.color.black_bottom));
                                            holder.unmrk_ans.setVisibility(View.GONE);
                                            holder.mrk_ans.setVisibility(View.VISIBLE);

                                        })
                                        .addOnFailureListener(e -> {
                                            mDialog.dismiss();
                                            Log.e("Error",e.getLocalizedMessage());
                                            Toasty.error(context, "Error unmarking as answer: "+e.getLocalizedMessage(), Toasty.LENGTH_SHORT,true).show();
                                        });

                            })
                            .addOnFailureListener(e -> {
                                mDialog.dismiss();
                                Log.e("Error",e.getLocalizedMessage());
                                Toasty.error(context, "Error unmarking as answer: "+e.getLocalizedMessage(), Toasty.LENGTH_SHORT,true).show();
                            });



                })
                .onNegative((dialog, which) -> dialog.dismiss()).show());

        holder.delete.setOnClickListener(v ->
                new MaterialDialog.Builder(context)
                .title("Delete")
                .content("Are you sure do you want to delete it?")
                .positiveText("Yes")
                .negativeText("No")
                .onPositive((dialog, which) -> {

                    dialog.dismiss();
                    final ProgressDialog mDialog = new ProgressDialog(context);
                    mDialog.setMessage("Please wait....");
                    mDialog.setIndeterminate(true);
                    mDialog.setCancelable(false);
                    mDialog.setCanceledOnTouchOutside(false);
                    mDialog.show();

                    if(!TextUtils.isEmpty(answered_by)) {

                        Map<String, Object> map1 = new HashMap<>();
                        map1.put("is_answer", "no");

                        FirebaseFirestore.getInstance()
                                .collection("Answers")
                                .document(answer.Answers_doc_id)
                                .update(map1)
                                .addOnSuccessListener(aVoid -> {

                                    Map<String, Object> map2 = new HashMap<>();
                                    map2.put("answered_by", "");
                                    map2.put("answered_by_id", "");

                                    FirebaseFirestore.getInstance()
                                            .collection("Questions")
                                            .document(answer.getQuestion_id())
                                            .update(map2)
                                            .addOnSuccessListener(aVoid12 -> {
                                                FirebaseFirestore.getInstance()
                                                        .collection("Answers")
                                                        .document(answer.Answers_doc_id)
                                                        .delete()
                                                        .addOnSuccessListener(aVoid1 -> {
                                                            mDialog.dismiss();
                                                            answereds.remove(holder.getAdapterPosition());
                                                            notifyDataSetChanged();
                                                            Toasty.success(context, "Deleted", Toasty.LENGTH_SHORT,true).show();

                                                        })
                                                        .addOnFailureListener(e -> {
                                                            mDialog.dismiss();
                                                            Log.e("Error",e.getLocalizedMessage());
                                                            Toasty.error(context, "Error deleting: "+e.getLocalizedMessage(), Toasty.LENGTH_SHORT,true).show();
                                                        });

                                            })
                                            .addOnFailureListener(e -> {
                                                mDialog.dismiss();
                                                Log.e("Error", e.getLocalizedMessage());
                                                Toasty.error(context, "Error unmarking as answer: " + e.getLocalizedMessage(), Toasty.LENGTH_SHORT, true).show();
                                            });

                                })
                                .addOnFailureListener(e -> {
                                    mDialog.dismiss();
                                    Log.e("Error", e.getLocalizedMessage());
                                    Toasty.error(context, "Error unmarking as answer: " + e.getLocalizedMessage(), Toasty.LENGTH_SHORT, true).show();
                                });


                    }else{

                        FirebaseFirestore.getInstance()
                                .collection("Answers")
                                .document(answer.Answers_doc_id)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    mDialog.dismiss();
                                    answereds.remove(holder.getAdapterPosition());
                                    notifyDataSetChanged();
                                    Toasty.success(context, "Deleted", Toasty.LENGTH_SHORT,true).show();

                                })
                                .addOnFailureListener(e -> {
                                    mDialog.dismiss();
                                    Log.e("Error",e.getLocalizedMessage());
                                    Toasty.error(context, "Error deleting: "+e.getLocalizedMessage(), Toasty.LENGTH_SHORT,true).show();
                                });

                    }

                })
                .onNegative((dialog, which) -> dialog.dismiss()).show());

        if(owner_id.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {

            holder.itemView.setOnLongClickListener(v -> {

                new MaterialDialog.Builder(context)
                        .title("Delete")
                        .content("Are you sure do you want to delete it?")
                        .positiveText("Yes")
                        .negativeText("No")
                        .onPositive((dialog, which) -> {

                            dialog.dismiss();
                            final ProgressDialog mDialog = new ProgressDialog(context);
                            mDialog.setMessage("Please wait....");
                            mDialog.setIndeterminate(true);
                            mDialog.setCancelable(false);
                            mDialog.setCanceledOnTouchOutside(false);
                            mDialog.show();

                            if(!TextUtils.isEmpty(answered_by)) {

                                Map<String, Object> map1 = new HashMap<>();
                                map1.put("is_answer", "no");

                                FirebaseFirestore.getInstance()
                                        .collection("Answers")
                                        .document(answer.Answers_doc_id)
                                        .update(map1)
                                        .addOnSuccessListener(aVoid -> {

                                            Map<String, Object> map2 = new HashMap<>();
                                            map2.put("answered_by", "");
                                            map2.put("answered_by_id", "");

                                            FirebaseFirestore.getInstance()
                                                    .collection("Questions")
                                                    .document(answer.getQuestion_id())
                                                    .update(map2)
                                                    .addOnSuccessListener(aVoid12 -> {
                                                        FirebaseFirestore.getInstance()
                                                                .collection("Answers")
                                                                .document(answer.Answers_doc_id)
                                                                .delete()
                                                                .addOnSuccessListener(aVoid1 -> {
                                                                    mDialog.dismiss();
                                                                    answereds.remove(holder.getAdapterPosition());
                                                                    notifyDataSetChanged();
                                                                    Toasty.success(context, "Deleted", Toasty.LENGTH_SHORT,true).show();

                                                                })
                                                                .addOnFailureListener(e -> {
                                                                    mDialog.dismiss();
                                                                    Log.e("Error",e.getLocalizedMessage());
                                                                    Toasty.error(context, "Error deleting: "+e.getLocalizedMessage(), Toasty.LENGTH_SHORT,true).show();
                                                                });

                                                    })
                                                    .addOnFailureListener(e -> {
                                                        mDialog.dismiss();
                                                        Log.e("Error", e.getLocalizedMessage());
                                                        Toasty.error(context, "Error unmarking as answer: " + e.getLocalizedMessage(), Toasty.LENGTH_SHORT, true).show();
                                                    });

                                        })
                                        .addOnFailureListener(e -> {
                                            mDialog.dismiss();
                                            Log.e("Error", e.getLocalizedMessage());
                                            Toasty.error(context, "Error unmarking as answer: " + e.getLocalizedMessage(), Toasty.LENGTH_SHORT, true).show();
                                        });


                            }else{

                                FirebaseFirestore.getInstance()
                                        .collection("Answers")
                                        .document(answer.Answers_doc_id)
                                        .delete()
                                        .addOnSuccessListener(aVoid -> {
                                            mDialog.dismiss();
                                            answereds.remove(holder.getAdapterPosition());
                                            notifyDataSetChanged();
                                            Toasty.success(context, "Deleted", Toasty.LENGTH_SHORT,true).show();

                                        })
                                        .addOnFailureListener(e -> {
                                            mDialog.dismiss();
                                            Log.e("Error",e.getLocalizedMessage());
                                            Toasty.error(context, "Error deleting: "+e.getLocalizedMessage(), Toasty.LENGTH_SHORT,true).show();
                                        });

                            }

                        }).onNegative((dialog, which) -> dialog.dismiss()).show();

                return true;
            });

        }
    }

    @Override
    public int getItemCount() {
        return answereds.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout item;
        FrameLayout bottom;
        TextView answer,name,timestamp;
        Button mrk_ans,unmrk_ans;
        ImageButton delete;
        CircleImageView profile_pic;

        ViewHolder(View itemView) {
            super(itemView);

            item=itemView.findViewById(R.id.layout);
            bottom=itemView.findViewById(R.id.bottom);
            delete=itemView.findViewById(R.id.delete);
            mrk_ans=itemView.findViewById(R.id.mrk_ans);
            unmrk_ans=itemView.findViewById(R.id.unmrk_ans);
            answer=itemView.findViewById(R.id.answer);
            name=itemView.findViewById(R.id.name);
            timestamp=itemView.findViewById(R.id.timestamp);
            profile_pic=itemView.findViewById(R.id.profile_pic);

        }
    }
}