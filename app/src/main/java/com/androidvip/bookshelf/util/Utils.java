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
import com.androidvip.bookshelf.model.Livro;
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

    public static String estadoLeituraToString(int estadoLeitura, Context context) {
        String ret;
        switch (estadoLeitura) {
            case Livro.ESTADO_LENDO:
                ret = context.getString(R.string.estado_leitura_lendo);
                break;
            case Livro.ESTADO_DESEJADO:
                ret = context.getString(R.string.estado_leitura_desejo);
                break;
            case Livro.ESTADO_EM_ESPERA:
                ret = context.getString(R.string.estado_leitura_em_espera);
                break;
            case Livro.ESTADO_DESISTIDO:
                ret = context.getString(R.string.estado_leitura_desistido);
                break;
            case Livro.ESTADO_FINALIZADO:
                ret = context.getString(R.string.estado_leitura_finalizado);
                break;
            default:
                ret = context.getString(R.string.add_lista);
                break;
        }
        return ret;
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

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null)
            view = new View(activity);
        if (imm != null)
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static boolean isOnline(Context contexto) {
        ConnectivityManager cm = (ConnectivityManager)contexto.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo redeAtiva = cm.getActiveNetworkInfo();
        return redeAtiva != null && redeAtiva.isConnectedOrConnecting();
    }

    public static String notNull(@Nullable String s, @NonNull String defaultValue) {
        return s == null || s.trim().equals("null") || s.trim().equals("")  ? defaultValue : s;
    }
}
