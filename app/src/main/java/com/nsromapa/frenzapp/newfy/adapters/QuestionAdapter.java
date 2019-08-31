package com.nsromapa.frenzapp.newfy.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;

import com.nsromapa.frenzapp.newfy.R;
import com.nsromapa.frenzapp.newfy.ui.activities.forum.AnswersActivity;
import com.nsromapa.frenzapp.newfy.models.AllQuestionsModel;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.ViewHolder> {

    private List<AllQuestionsModel> allQuestionsModels;
    private Context context;

    public QuestionAdapter(List<AllQuestionsModel> unanswereds) {
        this.allQuestionsModels = unanswereds;
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
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_question,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        AllQuestionsModel allQuestionsModel = allQuestionsModels.get(holder.getAdapterPosition());

        if(allQuestionsModel.getId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){

            holder.item.setOnLongClickListener(v -> {
                new MaterialDialog.Builder(context)
                        .title("Delete")
                        .content("Are you sure do you want to delete this question?")
                        .positiveText("Yes")
                        .negativeText("No")
                        .onPositive((dialog, which) -> {
                            dialog.dismiss();

                            final ProgressDialog mDialog=new ProgressDialog(context);
                            mDialog.setMessage("Please wait....");
                            mDialog.setIndeterminate(true);
                            mDialog.setCancelable(false);
                            mDialog.setCanceledOnTouchOutside(false);
                            mDialog.show();

                            FirebaseFirestore.getInstance().collection("Questions")
                                    .document(allQuestionsModel.question_doc_id)
                                    .delete().addOnSuccessListener(aVoid -> {
                                        mDialog.dismiss();
                                        allQuestionsModels.remove(holder.getAdapterPosition());
                                        Toasty.success(context, "Question Deleted", Toasty.LENGTH_SHORT, true).show();
                                        notifyDataSetChanged();
                                    })
                                    .addOnFailureListener(e -> {
                                        mDialog.dismiss();
                                        Log.e("Error",e.getLocalizedMessage());
                                    });

                        })
                        .onNegative((dialog, which) -> dialog.dismiss())
                        .show();
                return false;
            });

        }

        holder.question.setText(allQuestionsModel.getQuestion());
        holder.timestamp.setText(TimeAgo.using(Long.parseLong(allQuestionsModel.getTimestamp())));

        if(TextUtils.isEmpty(allQuestionsModel.getAnswered_by())||allQuestionsModel.getAnswered_by().equals("")){
            holder.answered_by.setVisibility(View.GONE);
        }else{
            holder.answered_by.setText(allQuestionsModel.getAnswered_by());
        }

        holder.subject.setText(String.format(" %s ", allQuestionsModel.getSubject()));
        holder.author.setText(String.format(" %s ", allQuestionsModel.getName()));

        FirebaseFirestore.getInstance().collection("Users")
                .document(allQuestionsModel.getId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    try {
                        if (!documentSnapshot.getString("name").equals(allQuestionsModel.getName())) {

                            Map<String, Object> map = new HashMap<>();
                            map.put("name", documentSnapshot.getString("name"));

                            FirebaseFirestore.getInstance().collection("Answers")
                                    .document(allQuestionsModel.question_doc_id)
                                    .update(map)
                                    .addOnSuccessListener(aVoid -> holder.author.setText(String.format(" %s ", documentSnapshot.getString("name"))));

                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                });

        /*holder.item.setOnClickListener(v -> context.startActivity(new Intent(context, AnswersActivity.class)
                .putExtra("answered_by", allQuestionsModels.get(holder.getAdapterPosition()).getAnswered_by())
                .putExtra("user_id", allQuestionsModels.get(holder.getAdapterPosition()).getId())
                .putExtra("doc_id", allQuestionsModels.get(holder.getAdapterPosition()).question_doc_id)
                .putExtra("author", allQuestionsModels.get(holder.getAdapterPosition()).getName())
                .putExtra("question", allQuestionsModels.get(holder.getAdapterPosition()).getQuestion())
                .putExtra("timestamp", allQuestionsModels.get(holder.getAdapterPosition()).getTimestamp())));*/

        holder.item.setOnClickListener(v -> AnswersActivity.startActivity(context,allQuestionsModel.question_doc_id));

    }

    @Override
    public int getItemCount() {
        return allQuestionsModels.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView question,timestamp,author,subject,answered_by;
        LinearLayout item;

        ViewHolder(View itemView) {
            super(itemView);

            item=itemView.findViewById(R.id.layout);
            question=itemView.findViewById(R.id.question);
            timestamp=itemView.findViewById(R.id.timestamp);
            author=itemView.findViewById(R.id.author);
            subject=itemView.findViewById(R.id.subject);
            answered_by=itemView.findViewById(R.id.answered_by);

        }
    }
}