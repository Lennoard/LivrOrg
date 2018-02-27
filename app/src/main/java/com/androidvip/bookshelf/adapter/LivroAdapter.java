package com.androidvip.bookshelf.adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.androidvip.bookshelf.App;
import com.androidvip.bookshelf.R;
import com.androidvip.bookshelf.activity.DetalhesActivity;
import com.androidvip.bookshelf.model.Book;
import com.androidvip.bookshelf.util.Utils;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.books.model.Volume;
import com.squareup.picasso.Picasso;

import java.util.List;

import io.objectbox.Box;

public class LivroAdapter extends RecyclerView.Adapter<LivroAdapter.ViewHolder> {
    private Activity activity;
    private List<Book> mDataSet;
    private Box<Book> livroBox;
    private boolean livrosFinalizados;
    private CoordinatorLayout cl;

    public LivroAdapter(Activity activity, List<Book> list, boolean livrosFinalizados) {
        this.activity = activity;
        this.livrosFinalizados = livrosFinalizados;
        mDataSet = list;
        cl = activity.findViewById(R.id.cl);
        livroBox = ((App) activity.getApplication()).getBoxStore().boxFor(Book.class);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView titulo, autores, data;
        RatingBar nota;
        ImageView capa;
        RelativeLayout cardLayout, notaLayout;

        ViewHolder(View v){
            super(v);
            titulo = v.findViewById(R.id.lista_titulo);
            autores = v.findViewById(R.id.lista_autores);
            data = v.findViewById(R.id.lista_data);
            nota = v.findViewById(R.id.lista_nota);
            capa = v.findViewById(R.id.lista_capa_livro);
            cardLayout = v.findViewById(R.id.card_layout);
            notaLayout = v.findViewById(R.id.lista_layout_nota);
        }
    }

    @Override
    public LivroAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(activity).inflate(R.layout.item_lista_livro, parent,false);
        return new LivroAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final LivroAdapter.ViewHolder holder, int position) {
        holder.capa.setImageResource(R.drawable.carregando_imagem);
        Book book = mDataSet.get(position);

        new Thread(() -> {
            try {
                Volume volume = Utils.getVolume(book.getGoogleBooksId());
                activity.runOnUiThread(() -> {
                    if (volume.getVolumeInfo().getImageLinks() != null)
                        Picasso.with(activity)
                                .load(volume.getVolumeInfo().getImageLinks().getThumbnail())
                                .placeholder(R.drawable.carregando_imagem)
                                .error(R.drawable.broken_image)
                                .into(holder.capa);
                    else
                        holder.capa.setImageResource(R.drawable.broken_image);
                });
            } catch (Exception e){
                activity.runOnUiThread(() -> holder.capa.setImageResource(R.drawable.broken_image));
            }
        }).start();

        holder.titulo.setText(book.getTitle());
        holder.autores.setText(book.getAuthors());
        holder.nota.setRating(book.getScore());
        holder.data.setText(livrosFinalizados
                ? Utils.dateToString(book.getReadingEndDate())
                : Utils.dateToString(book.getReadingStartDate())
        );

        holder.cardLayout.setOnLongClickListener(v -> {
            PopupMenu popup = new PopupMenu(activity, holder.cardLayout);
            popup.getMenuInflater().inflate(R.menu.popup, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()){
                    case R.id.popup_remover:
                        Book bookParaRemover = mDataSet.get(position);
                        new AlertDialog.Builder(activity)
                                .setTitle(android.R.string.dialog_alert_title)
                                .setMessage(activity.getString(R.string.aviso_remover_livro, book.getTitle()))
                                .setPositiveButton(android.R.string.yes, (dialog, which) -> removerLivro(bookParaRemover, position))
                                .setNegativeButton(android.R.string.cancel, (dialog, which) -> {})
                                .show();
                        break;
                    case R.id.popup_books:
                        Utils.webPage(activity, "https://books.google.com.br/books?id=" + book.getGoogleBooksId());
                        break;
                }
                return true;
            });
            popup.show();
            return true;
        });

        holder.cardLayout.setOnClickListener(v -> {
            Intent intent = new Intent(activity, DetalhesActivity.class);
            intent.putExtra("livroId", livroBox.getId(book));
            activity.startActivity(intent);
        });

        holder.notaLayout.setOnClickListener(v -> {
            int checkedItem = book.getScore() == 0 ? -1 : book.getScore() -1;
            String[] notas = activity.getResources().getStringArray(R.array.notas_array);
            new AlertDialog.Builder(activity)
                    .setTitle(R.string.nota)
                    .setSingleChoiceItems(notas, checkedItem, (dialog, which) -> {
                        book.setScore(which + 1);
                        livroBox.put(book);
                        holder.nota.setRating(which + 1);
                        dialog.dismiss();
                    }).show();
        });
    }

    private void removerLivro(Book book, int position) {
        livroBox.remove(book);
        mDataSet.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount());
        Snackbar.make(cl, activity.getString(R.string.item_removido, book.getTitle()), Snackbar.LENGTH_LONG)
                .setAction(R.string.desfazer, v1 -> addLivro(book)).show();
    }

    private void addLivro(Book book) {
        livroBox.put(book);
        mDataSet.add(book);
        notifyDataSetChanged();
        Snackbar.make(cl, activity.getString(R.string.item_adicionado, book.getTitle()), Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public int getItemCount(){
        return mDataSet == null ? 0 : mDataSet.size();
    }

}

