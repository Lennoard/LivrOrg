package com.androidvip.bookshelf.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.androidvip.bookshelf.App;
import com.androidvip.bookshelf.R;
import com.androidvip.bookshelf.model.Livro;
import com.google.api.services.books.model.Volume;

import java.net.URL;
import java.util.List;

import io.objectbox.Box;

public class VolumeAdapter extends RecyclerView.Adapter<VolumeAdapter.ViewHolder> {
    private Activity activity;
    private List<Volume> mDataSet;
    private Box<Livro> livroBox;

    public VolumeAdapter(Activity activity, List<Volume> list) {
        this.activity = activity;
        mDataSet = list;
        livroBox = ((App) activity.getApplication()).getBoxStore().boxFor(Livro.class);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView titulo, autores;
        RatingBar classificacao;
        ImageView capa;

        ViewHolder(View v){
            super(v);
            titulo = v.findViewById(R.id.lista_titulo);
            autores = v.findViewById(R.id.lista_autores);
            classificacao = v.findViewById(R.id.lista_classificacao);
            capa = v.findViewById(R.id.lista_capa_livro);
        }
    }

    @Override
    public VolumeAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(activity).inflate(R.layout.item_lista_volume, parent,false);
        return new VolumeAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final VolumeAdapter.ViewHolder holder, int position) {
        holder.capa.setImageResource(R.drawable.carregando_imagem);
        Volume volume = mDataSet.get(position);
        Volume.VolumeInfo volumeInfo = volume.getVolumeInfo();

        if (volumeInfo != null) {
            holder.titulo.setText(volumeInfo.getTitle());
            holder.autores.setText(volumeInfo.getAuthors() == null ? "" : TextUtils.join(", ", volumeInfo.getAuthors()));

            holder.classificacao.setRating(volumeInfo.getAverageRating() == null
                    ? 0F : Float.parseFloat(volumeInfo.getAverageRating().toString()));

            if (volumeInfo.getImageLinks() != null)
                carregarImagem(activity, volumeInfo.getImageLinks().getThumbnail(), holder.capa);

            holder.itemView.setOnLongClickListener(v -> {
                String titulo = volumeInfo.getTitle();
                Livro livro = new Livro();
                livro.setTitulo(titulo == null ? "" : titulo);
                livro.setAutores(volumeInfo.getAuthors() == null ? "" : TextUtils.join(", ", volumeInfo.getAuthors()));
                livro.setGoogleBooksId(volume.getId());
                livro.setEstadoLeitura(Livro.ESTADO_DESEJADO);
                long id = livroBox.put(livro);

                Snackbar.make(holder.titulo, activity.getString(R.string.item_adicionado, titulo), Snackbar.LENGTH_LONG)
                        .setAction(R.string.desfazer, view -> {
                            livroBox.remove(id);
                            Snackbar.make(holder.titulo, activity.getString(R.string.item_removido, titulo), Snackbar.LENGTH_SHORT).show();
                        });
                return true;
            });
        }
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

