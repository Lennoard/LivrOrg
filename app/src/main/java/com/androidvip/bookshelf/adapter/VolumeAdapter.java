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
import com.androidvip.bookshelf.activity.BookDetailsActivity;
import com.androidvip.bookshelf.model.Book;
import com.androidvip.bookshelf.util.K;
import com.androidvip.bookshelf.util.Utils;
import com.google.api.services.books.model.Volume;
import com.squareup.picasso.Picasso;

import java.util.List;

import io.objectbox.Box;

public class VolumeAdapter extends RecyclerView.Adapter<VolumeAdapter.ViewHolder> {
    private Activity activity;
    private List<Volume> mDataSet;
    private Box<Book> bookBox;
    private CoordinatorLayout cl;

    public VolumeAdapter(Activity activity, List<Volume> list) {
        this.activity = activity;
        mDataSet = list;
        bookBox = ((App) activity.getApplication()).getBoxStore().boxFor(Book.class);
        // Snackbars view
        cl = activity.findViewById(R.id.cl);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, authors;
        RatingBar rating;
        ImageView cover;
        RelativeLayout cardLayout;

        ViewHolder(View v){
            super(v);
            title = v.findViewById(R.id.list_book_title);
            authors = v.findViewById(R.id.list_book_authors);
            rating = v.findViewById(R.id.lista_classificacao);
            cover = v.findViewById(R.id.list_book_cover);
            cardLayout = v.findViewById(R.id.list_book_card_layout);
        }
    }

    @Override
    public VolumeAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(activity).inflate(R.layout.list_item_volume, parent,false);
        return new VolumeAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final VolumeAdapter.ViewHolder holder, int position) {
        holder.cover.setImageResource(R.drawable.loading_image);
        Volume volume = mDataSet.get(position);
        Volume.VolumeInfo volumeInfo = volume.getVolumeInfo();

        if (volumeInfo != null) {
            holder.title.setText(volumeInfo.getTitle());
            holder.authors.setText(volumeInfo.getAuthors() == null ? "" : TextUtils.join(", ", volumeInfo.getAuthors()));

            holder.rating.setRating(volumeInfo.getAverageRating() == null
                    ? 0F : Float.parseFloat(volumeInfo.getAverageRating().toString()));

            if (volumeInfo.getImageLinks() != null)
                Picasso.with(activity)
                        .load(volumeInfo.getImageLinks().getThumbnail())
                        .placeholder(R.drawable.loading_image)
                        .error(R.drawable.broken_image)
                        .into(holder.cover);
            else
                holder.cover.setImageResource(R.drawable.broken_image);

            // Instantly adds the book to the wish list
            holder.cardLayout.setOnLongClickListener(v -> {
                String title = volumeInfo.getTitle();
                Book book = new Book();
                book.setTitle(Utils.notNull(title, ""));
                book.setAuthors(TextUtils.join(", ", volumeInfo.getAuthors()));
                book.setGoogleBooksId(volume.getId());
                book.setReadingState(Book.STATE_WISH);
                long id = bookBox.put(book);

                Snackbar.make(cl, activity.getString(R.string.item_added_wish, title), Snackbar.LENGTH_LONG)
                        .setAction(R.string.undo, view -> {
                            bookBox.remove(id);
                            Snackbar.make(cl, activity.getString(R.string.item_removed, title), Snackbar.LENGTH_SHORT).show();
                        }).show();
                return true;
            });

            // Opens the details activity
            holder.cardLayout.setOnClickListener(v -> {
                Intent intent = new Intent(activity, BookDetailsActivity.class);
                intent.putExtra(K.EXTRA_VOLUME_ID, volume.getId());
                activity.startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount(){
        return mDataSet == null ? 0 : mDataSet.size();
    }

}

