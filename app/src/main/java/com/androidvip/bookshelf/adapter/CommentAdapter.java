package com.androidvip.bookshelf.adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.androidvip.bookshelf.App;
import com.androidvip.bookshelf.R;
import com.androidvip.bookshelf.activity.CommentDetailsActivity;
import com.androidvip.bookshelf.model.Comment;
import com.androidvip.bookshelf.util.K;
import com.androidvip.bookshelf.util.Utils;

import java.util.List;

import io.objectbox.Box;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    private Activity activity;
    private List<Comment> mDataSet;
    private Box<Comment> commentBox;
    private CoordinatorLayout cl;

    public CommentAdapter(Activity activity, List<Comment> list) {
        this.activity = activity;
        mDataSet = list;
        cl = activity.findViewById(R.id.cl);
        commentBox = ((App) activity.getApplication()).getBoxStore().boxFor(Comment.class);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, date, location;
        RelativeLayout itemLayout;

        ViewHolder(View v){
            super(v);
            title = v.findViewById(R.id.list_comment_title);
            date = v.findViewById(R.id.list_comment_date);
            location = v.findViewById(R.id.list_comment_location);
            itemLayout = v.findViewById(R.id.list_comment_layout);
        }
    }

    @NonNull
    @Override
    public CommentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(activity).inflate(R.layout.item_list_comment, parent,false);
        return new CommentAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final CommentAdapter.ViewHolder holder, int position) {
        Comment comment = mDataSet.get(position);

        holder.title.setText(comment.getTitle());
        holder.date.setText(Utils.dateToString(comment.getDate()));
        holder.location.setText(buildCommentLocation(comment.getChapter(), comment.getPage()));

        holder.itemLayout.setOnClickListener(v -> {
            Intent intent = new Intent(activity, CommentDetailsActivity.class);
            intent.putExtra(K.EXTRA_COMMENT_ID, comment.getId());
            activity.startActivity(intent);
        });

        holder.itemLayout.setOnLongClickListener(v -> {
            new AlertDialog.Builder(activity)
                    .setTitle(android.R.string.dialog_alert_title)
                    .setMessage(R.string.alert_remove_comment)
                    .setNegativeButton(android.R.string.no, (dialog, which) -> {})
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        commentBox.remove(comment);
                        mDataSet.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, getItemCount());
                        Snackbar.make(cl, activity.getString(R.string.item_removed, comment.getTitle()), Snackbar.LENGTH_LONG)
                                .setAction(R.string.undo, v1 -> {
                                    commentBox.put(comment);
                                    mDataSet.add(comment);
                                    notifyDataSetChanged();
                                }).show();
                    })
                    .show();
            return true;
        });

    }

    @Override
    public int getItemCount(){
        return mDataSet == null ? 0 : mDataSet.size();
    }

    /**
     * Generates a quick and single display of the comment's location and page.
     * It will also add 0 before the number if it is smaller than 10 for convenience
     *
     * @return The comment's "location" reference in the format Cxx/Pxx
     * e.g C05/P109
     */
    private String buildCommentLocation(int ch, int pg) {
        String chapter, page;
        chapter = ch < 10 ? "0" + String.valueOf(ch) : String.valueOf(ch);
        page    = pg < 10 ? "0" + String.valueOf(pg) : String.valueOf(pg);

        return "C" + chapter + "/P" + page;
    }

}

