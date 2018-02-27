package com.androidvip.bookshelf.adapter;

import android.app.Activity;
import android.content.Intent;
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
import com.androidvip.bookshelf.activity.ComentariosDetalhesActivity;
import com.androidvip.bookshelf.model.Comment;
import com.androidvip.bookshelf.util.Utils;

import java.util.List;

import io.objectbox.Box;

public class ComentarioAdapter extends RecyclerView.Adapter<ComentarioAdapter.ViewHolder> {
    private Activity activity;
    private List<Comment> mDataSet;
    private Box<Comment> comentarioBox;
    private CoordinatorLayout cl;

    public ComentarioAdapter(Activity activity, List<Comment> list) {
        this.activity = activity;
        mDataSet = list;
        cl = activity.findViewById(R.id.cl);
        comentarioBox = ((App) activity.getApplication()).getBoxStore().boxFor(Comment.class);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView titulo, data, capitulo;
        RelativeLayout itemLayout;

        ViewHolder(View v){
            super(v);
            titulo = v.findViewById(R.id.comentarios_titulo);
            data = v.findViewById(R.id.comentarios_data);
            capitulo = v.findViewById(R.id.comentarios_cap);
            itemLayout = v.findViewById(R.id.comentarios_item_layout);
        }
    }

    @Override
    public ComentarioAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(activity).inflate(R.layout.item_lista_comentario, parent,false);
        return new ComentarioAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ComentarioAdapter.ViewHolder holder, int position) {
        Comment comment = mDataSet.get(position);

        holder.titulo.setText(comment.getTitle());
        holder.data.setText(Utils.dateToString(comment.getDate()));
        holder.capitulo.setText(capBuilder(comment.getChapter(), comment.getPage()));

        holder.itemLayout.setOnClickListener(v -> {
            Intent intent = new Intent(activity, ComentariosDetalhesActivity.class);
            intent.putExtra("id", comment.getId());
            activity.startActivity(intent);
        });

        holder.itemLayout.setOnLongClickListener(v -> {
            new AlertDialog.Builder(activity)
                    .setTitle(android.R.string.dialog_alert_title)
                    .setMessage(R.string.aviso_remover_comentario)
                    .setNegativeButton(android.R.string.no, (dialog, which) -> {})
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        comentarioBox.remove(comment);
                        mDataSet.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, getItemCount());
                        Snackbar.make(cl, activity.getString(R.string.item_removido, comment.getTitle()), Snackbar.LENGTH_LONG)
                                .setAction(R.string.desfazer, v1 -> {
                                    comentarioBox.put(comment);
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

    private String capBuilder(int cap, int pag) {
        String capitulo, pagina;
        capitulo = cap < 10 ? "0" + String.valueOf(cap) : String.valueOf(cap);
        pagina   = pag < 10 ? "0" + String.valueOf(pag) : String.valueOf(pag);

        return "C" + capitulo + "/P" + pagina;
    }

}

