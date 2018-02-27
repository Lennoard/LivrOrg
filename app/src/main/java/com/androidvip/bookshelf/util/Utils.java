package com.androidvip.bookshelf.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.androidvip.bookshelf.R;
import com.androidvip.bookshelf.model.Book;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.books.Books;
import com.google.api.services.books.BooksRequestInitializer;
import com.google.api.services.books.model.Volume;
import com.google.api.services.books.model.Volumes;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Utility class. Its methods are often used statically through the whole Java codes,
 * thus, we might need not to supply {@link Context} in those methods
 * Nobody shall inherit from it.
 */
public final class Utils {

    private Utils() {
        // Do not let anyone instantiate this class
    }

    /**
     * Searches for volumes given a query. The query must built with a valid prefix.
     * Do not call this method from the main thread
     *
     * @param query The string representing the query used to search through the Books API database
     * @return A list of volumes that matches the query
     * @throws IOException if the listing fails on initializing the Google HTTP Client Request
     * @throws IllegalArgumentException if the query does not contain a valid prefix
     */
    public static List<Volume> searchVolumes(String query) throws IOException, IllegalArgumentException {
        if (!query.startsWith("intitle:") && !query.startsWith("inauthor:") && !query.startsWith("isbn:"))
            throw new IllegalArgumentException("The query must contain a prefix (use intitle, inauthor or isbn)");

        Books.Volumes.List volumeList = booksReference().volumes().list(query);
        volumeList.setMaxResults(40L);
        volumeList.setOrderBy("relevance");
        Volumes volumes = volumeList.execute();

        return volumes.getItems();
    }

    /**
     * Obtain a single volume given an id. Do not call this method from the main thread.
     *
     * @param volumeId an unique identifier for a volume
     * @return a Volume associated with the id
     * @throws IOException if the listing fails on initializing the Google HTTP Client Request
     */
    public static Volume getVolume(String volumeId) throws IOException {
       return  booksReference().volumes().get(volumeId).execute();
    }

    /**
     * Converts a book reading state to a human-readable String
     *
     * @param readingState the book's reading state
     * @param context the context used to get the resource string
     * @return a String corresponding to the reading state
     */
    public static String readingStateToString(int readingState, Context context) {
        String ret;
        switch (readingState) {
            case Book.STATE_READING:  ret = context.getString(R.string.estado_leitura_lendo); break;
            case Book.STATE_WISH:     ret = context.getString(R.string.estado_leitura_desejo); break;
            case Book.STATE_ON_HOLD:  ret = context.getString(R.string.estado_leitura_em_espera); break;
            case Book.STATE_DROPPED:  ret = context.getString(R.string.estado_leitura_desistido); break;
            case Book.STATE_FINISHED: ret = context.getString(R.string.estado_leitura_finalizado); break;
            default: ret = context.getString(R.string.add_lista);
        }
        return ret;
    }

    /**
     * Coverts java.util.Date to dd/MM/yyyy format e.g 08/03/1992
     *
     * @param date the Date reference
     * @return an empty String if {@param date} is illegal, the formatted String otherwise
     */
    public static String dateToString(Date date) {
        String s = "";
        try  {
            DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);
            s = df.format(date);
        } catch (Exception ignored) {

        }
        return s;
    }

    /**
     * Calls the user's web browser of choice with an intent
     * holding the URL to be loaded
     */
    public static void webPage(Context context, String url){
        Uri uri = Uri.parse(url);
        try {
            Intent i = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(i);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, context.getString(R.string.browser_failure) + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * One of the possible methods to hide the soft keyboard.
     * Used when displaying bottom information on the screen,
     * such as a {@link android.support.design.widget.Snackbar]
     *
     * @param activity the Activity that holds the currently focused view
     */

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null)
            view = new View(activity);
        if (imm != null)
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * Checks if the devices is connected to a network and
     * this network has connections stabilised
     *
     * @param context the Context used to get the connectivity service}
     * @return true if the device is online, false otherwise
     */
    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    /**
     * Gracefully deals with nullable Strings. should be used as much as possible
     * because {@link Volume} properties often return null
     *
     * @param o the Object to get the nullable String value
     * @param defaultValue the wanted/safe value in case of param "o" is actually null
     * @return the object's toString method or the param defaultValue in case of nullity
     * @throws IllegalArgumentException if the default value is null
     */
    public static String notNull(@Nullable Object o, String defaultValue) throws IllegalArgumentException{
        if (defaultValue == null)
            throw new IllegalArgumentException("The default value must not be null");
        if (o == null)
            return defaultValue;
        if (o instanceof String)
            return o.toString().trim().equals("") ? defaultValue : o.toString();
        else
            return String.valueOf(o);
    }

    private static Books booksReference() {
        return new Books.Builder(new ApacheHttpTransport(), JacksonFactory.getDefaultInstance(), null)
                .setApplicationName("Bookshelf-AndroidVIP")
                .setGoogleClientRequestInitializer(new BooksRequestInitializer(K.API_KEY))
                .build();
    }
}
