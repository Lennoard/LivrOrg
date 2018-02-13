package com.androidvip.bookshelf.util;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.widget.Toast;

import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.books.Books;
import com.google.api.services.books.BooksRequestInitializer;
import com.google.api.services.books.model.Volume;
import com.google.api.services.books.model.Volumes;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class Utils {
    private Utils() {

    }

    public static List<Volume> pesquisarVolumes(JsonFactory jsonFactory, String query) throws Exception {
        final Books books = new Books.Builder(new ApacheHttpTransport(), jsonFactory, null)
                .setApplicationName("LivrOrg-AndroidVIP")
                .setGoogleClientRequestInitializer(new BooksRequestInitializer(K.API_KEY))
                .build();

        Books.Volumes.List listaVolumes = books.volumes().list(query);
        listaVolumes.setMaxResults(40L);
        listaVolumes.setOrderBy("relevance");
        Volumes volumes = listaVolumes.execute();

        return volumes.getItems();
    }

    public static Volume obterVolume(JsonFactory jsonFactory, String volumeId) throws Exception {
        final Books books = new Books.Builder(new ApacheHttpTransport(), jsonFactory, null)
                .setApplicationName("Bookshelf-AndroidVIP")
                .setGoogleClientRequestInitializer(new BooksRequestInitializer(K.API_KEY))
                .build();

        Books.Volumes.Get get = books.volumes().get(volumeId);

        return get.execute();
    }

    public static String dateToString(Date date) {
        String s = "";
        try  {
            DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);
            s = df.format(date);
        } catch (Exception ignored) {

        }
        return s;
    }

    public static void webPage(Context context, String url){
        Uri uri = Uri.parse(url);
        try {
            Intent i = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(i);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, "Não foi possível iniciar o navegador: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean isOnline(Context contexto) {
        ConnectivityManager cm = (ConnectivityManager)contexto.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo redeAtiva = cm.getActiveNetworkInfo();
        return redeAtiva != null && redeAtiva.isConnectedOrConnecting();
    }
}
