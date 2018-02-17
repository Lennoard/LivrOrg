package com.androidvip.bookshelf.adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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
import com.androidvip.bookshelf.model.Livro;
import com.androidvip.bookshelf.util.Utils;
import com.google.api.services.books.model.Volume;
import com.squareup.picasso.Picasso;

import java.util.List;

import io.objectbox.Box;

public class VolumeAdapter extends RecyclerView.Adapter<VolumeAdapter.ViewHolder> {
    private Activity activity;
    private List<Volume> mDataSet;
    private Box<Livro> livroBox;
    private CoordinatorLayout cl;

    public VolumeAdapter(Activity activity, List<Volume> list) {
        this.activity = activity;
        mDataSet = list;
        livroBox = ((App) activity.getApplication()).getBoxStore().boxFor(Livro.class);
        cl = activity.findViewById(R.id.cl);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView titulo, autores;
        RatingBar classificacao;
        ImageView capa;
        RelativeLayout cardLayout;

        ViewHolder(View v){
            super(v);
            titulo = v.findViewById(R.id.lista_titulo);
            autores = v.findViewById(R.id.lista_autores);
            classificacao = v.findViewById(R.id.lista_classificacao);
            capa = v.findViewById(R.id.lista_capa_livro);
            cardLayout = v.findViewById(R.id.card_layout);
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
                Picasso.with(activity)
                        .load(volumeInfo.getImageLinks().getThumbnail())
                        .placeholder(R.drawable.carregando_imagem)
                        .error(R.drawable.broken_image)
                        .into(holder.capa);
            else
                Picasso.with(activity).load(R.drawable.broken_image).into(holder.capa);

            holder.cardLayout.setOnLongClickListener(v -> {
                String titulo = volumeInfo.getTitle();
                Livro livro = new Livro();
                livro.setTitulo(titulo == null ? "" : titulo);
                livro.setAutores(volumeInfo.getAuthors() == null ? "" : TextUtils.join(", ", volumeInfo.getAuthors()));
                livro.setGoogleBooksId(volume.getId());
                livro.setEstadoLeitura(Livro.ESTADO_DESEJADO);
                long id = livroBox.put(livro);

                Snackbar.make(cl, activity.getString(R.string.item_adicionado, titulo), Snackbar.LENGTH_LONG)
                        .setAction(R.string.desfazer, view -> {
                            livroBox.remove(id);
                            Snackbar.make(cl, activity.getString(R.string.item_removido, titulo), Snackbar.LENGTH_SHORT).show();
                        }).show();
                return true;
            });

            holder.cardLayout.setOnClickListener(v -> {
                Intent intent = new Intent(activity, DetalhesActivity.class);
                intent.putExtra("volumeId", volume.getId());
                activity.startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount(){
        return mDataSet == null ? 0 : mDataSet.size();
    }

}

