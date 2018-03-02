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
import com.androidvip.bookshelf.activity.BookDetailsActivity;
import com.androidvip.bookshelf.model.Book;
import com.androidvip.bookshelf.util.K;
import com.androidvip.bookshelf.util.Utils;
import com.google.api.services.books.model.Volume;
import com.squareup.picasso.Picasso;

import java.util.List;

import io.objectbox.Box;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.ViewHolder> {
    private Activity activity;
    private List<Book> mDataSet;
    private Box<Book> bookBox;
    private boolean finishedBooks;
    private CoordinatorLayout cl;

    public BookAdapter(Activity activity, List<Book> list, boolean finishedBooks) {
        this.activity = activity;
        this.finishedBooks = finishedBooks;
        mDataSet = list;
        bookBox = ((App) activity.getApplication()).getBoxStore().boxFor(Book.class);
        // Snackbars view
        cl = activity.findViewById(R.id.cl);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, authors, date;
        RatingBar rating;
        ImageView cover;
        RelativeLayout cardLayout, ratingLayout;

        ViewHolder(View v){
            super(v);
            title = v.findViewById(R.id.list_book_title);
            authors = v.findViewById(R.id.list_book_authors);
            date = v.findViewById(R.id.list_book_date);
            rating = v.findViewById(R.id.list_book_rating);
            cover = v.findViewById(R.id.list_book_cover);
            cardLayout = v.findViewById(R.id.list_book_card_layout);
            ratingLayout = v.findViewById(R.id.list_book_layout_rating);
        }
    }

    @Override
    public BookAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(activity).inflate(R.layout.list_item_book, parent,false);
        return new BookAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final BookAdapter.ViewHolder holder, int position) {
        holder.cover.setImageResource(R.drawable.loading_image);
        Book book = mDataSet.get(position);

        // Get volume form the book id once so we can fetch and display the book cover
        new Thread(() -> {
            try {
                Volume volume = Utils.getVolume(book.getGoogleBooksId());
                activity.runOnUiThread(() -> {
                    if (volume.getVolumeInfo().getImageLinks() != null)
                        Picasso.with(activity)
                                .load(volume.getVolumeInfo().getImageLinks().getThumbnail())
                                .placeholder(R.drawable.loading_image)
                                .error(R.drawable.broken_image)
                                .into(holder.cover);
                    else
                        holder.cover.setImageResource(R.drawable.broken_image);
                });
            } catch (Exception e){
                activity.runOnUiThread(() -> holder.cover.setImageResource(R.drawable.broken_image));
            }
        }).start();

        holder.title.setText(book.getTitle());
        holder.authors.setText(book.getAuthors());
        holder.rating.setRating(book.getScore());
        holder.date.setText(finishedBooks
                ? Utils.dateToString(book.getReadingEndDate())
                : Utils.dateToString(book.getReadingStartDate())
        );

        holder.cardLayout.setOnLongClickListener(v -> {
            PopupMenu popup = new PopupMenu(activity, holder.cardLayout);
            popup.getMenuInflater().inflate(R.menu.popup, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()){
                    case R.id.popup_remove_book:
                        Book bookToRemove = mDataSet.get(position);
                        new AlertDialog.Builder(activity)
                                .setTitle(android.R.string.dialog_alert_title)
                                .setMessage(activity.getString(R.string.aviso_remover_livro, book.getTitle()))
                                .setPositiveButton(android.R.string.yes, (dialog, which) -> removeBook(bookToRemove, position))
                                .setNegativeButton(android.R.string.cancel, (dialog, which) -> {})
                                .show();
                        break;
                    case R.id.popup_google_books:
                        Utils.webPage(activity, "https://books.google.com.br/books?id=" + book.getGoogleBooksId());
                        break;
                }
                return true;
            });
            popup.show();
            return true;
        });

        holder.cardLayout.setOnClickListener(v -> {
            Intent intent = new Intent(activity, BookDetailsActivity.class);
            intent.putExtra(K.EXTRA_BOOK_ID_DB, bookBox.getId(book));
            activity.startActivity(intent);
        });

        // Let the user set a score with a single choice dialog selection
        holder.ratingLayout.setOnClickListener(v -> {
            /*
            * The dialog's checked item. If the book has no reading state yet (0),
            * then use -1, which is the default value and checks nothing, otherwise
            * use the book's reading state -1 (reading states start from 1)
            */
            int checkedItem = book.getScore() == 0 ? -1 : book.getScore() - 1;
            String[] scores = activity.getResources().getStringArray(R.array.scores_array);
            new AlertDialog.Builder(activity)
                    .setTitle(R.string.score)
                    .setSingleChoiceItems(scores, checkedItem, (dialog, which) -> {
                        book.setScore(which + 1);
                        bookBox.put(book);
                        holder.rating.setRating(which + 1);
                        dialog.dismiss();
                    }).show();
        });
    }

    private void removeBook(Book bookToRemove, int position) {
        bookBox.remove(bookToRemove);
        mDataSet.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount());
        Snackbar.make(cl, activity.getString(R.string.item_removido, bookToRemove.getTitle()), Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, v1 -> addBook(bookToRemove)).show();
    }

    private void addBook(Book bookToAdd) {
        bookBox.put(bookToAdd);
        mDataSet.add(bookToAdd);
        notifyDataSetChanged();
        Snackbar.make(cl, activity.getString(R.string.item_adicionado, bookToAdd.getTitle()), Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public int getItemCount(){
        return mDataSet == null ? 0 : mDataSet.size();
    }

}

