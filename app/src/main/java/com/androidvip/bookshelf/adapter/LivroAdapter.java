package com.androidvip.bookshelf.adapter;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidvip.bookshelf.R;
import com.androidvip.bookshelf.model.Livro;
import com.androidvip.bookshelf.util.Utils;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.books.model.Volume;

import java.net.URL;
import java.util.List;

import io.objectbox.Box;

public class LivroAdapter extends RecyclerView.Adapter<LivroAdapter.ViewHolder> {
    private Activity activity;
    private List<Livro> mDataSet;
    private Box<Livro> livroBox;
    private boolean livrosFinalizados;
    private CoordinatorLayout cl;

    public LivroAdapter(Activity activity, Box<Livro> list, boolean livrosFinalizados) {
        this.activity = activity;
        this.livroBox = list;
        this.livrosFinalizados = livrosFinalizados;
        mDataSet = livroBox.getAll();
        cl = activity.findViewById(R.id.cl);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView titulo, autores, data, nota;
        ImageView capa;

        ViewHolder(View v){
            super(v);
            titulo = v.findViewById(R.id.lista_titulo);
            autores = v.findViewById(R.id.lista_autores);
            data = v.findViewById(R.id.lista_data);
            nota = v.findViewById(R.id.lista_nota);
            capa = v.findViewById(R.id.lista_capa_livro);
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
        Livro livro = mDataSet.get(position);

        new Thread(() -> {
            try {
                Volume volume = Utils.obterVolume(JacksonFactory.getDefaultInstance(), livro.getGoogleBooksId());
                if (volume.getVolumeInfo().getImageLinks() != null)
                    carregarImagem(activity, volume.getVolumeInfo().getImageLinks().getThumbnail(), holder.capa);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        holder.titulo.setText(livro.getTitulo());
        holder.autores.setText(livro.getAutores());
        holder.nota.setText(livro.getNota() == 0 ? "--/10" : String.valueOf(livro.getNota()) + "/10");
        holder.data.setText(livrosFinalizados
                ? Utils.dateToString(livro.getDataTerminoLeitura())
                : Utils.dateToString(livro.getDataInicioLeitura())
        );
        holder.itemView.setOnLongClickListener(v -> {
            PopupMenu popup = new PopupMenu(activity, holder.itemView);
            popup.getMenuInflater().inflate(R.menu.popup, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()){
                    case R.id.popup_remover:
                        Livro livroParaRemover = mDataSet.get(position);
                        new AlertDialog.Builder(activity)
                                .setTitle(android.R.string.dialog_alert_title)
                                .setMessage(activity.getString(R.string.aviso_remover_item, livro.getTitulo()))
                                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                                    removerLivro(livroParaRemover, position);
                                })
                                .setNegativeButton(android.R.string.cancel, (dialog, which) -> {})
                                .show();
                        break;
                    case R.id.popup_books:
                        Utils.paginaWeb(activity, "https://books.google.com.br/books?id=" + livro.getGoogleBooksId());
                        break;
                }
                return true;
            });
            popup.show();
            return true;
        });
    }

    private void removerLivro(Livro livro, int position) {
        livroBox.remove(mDataSet.get(position));
        mDataSet.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount());
        Snackbar.make(cl, activity.getString(R.string.item_removido, livro.getTitulo()), Snackbar.LENGTH_LONG)
                .setAction(R.string.desfazer, v1 -> addLivro(livro, position)).show();
    }

    private void addLivro(Livro livro, int position) {
        livroBox.put(livro);
        mDataSet.add(livro);
        notifyItemInserted(position);
        notifyItemRangeInserted(position, getItemCount());
        Snackbar.make(cl, activity.getString(R.string.item_adicionado, livro.getTitulo()), Snackbar.LENGTH_SHORT).show();
    }

    private synchronized void carregarImagem(Activity activity, String capaUrl, ImageView imageView) {
        if (capaUrl != null) {
            new Thread(() -> {
                URL url;
                final Bitmap capaBitmap;
                try {
                    url = new URL(capaUrl);
                    capaBitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    if (activity != null) {
                        activity.runOnUiThread(() -> imageView.setImageDrawable(new BitmapDrawable(activity.getResources(), capaBitmap)));
                    }
                } catch (Exception ignored) {

                }
            }).start();
        }
    }

    @Override
    public int getItemCount(){
        return mDataSet == null ? 0 : mDataSet.size();
    }

}
