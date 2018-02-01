package com.androidvip.bookshelf.util;

import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.books.Books;
import com.google.api.services.books.BooksRequestInitializer;
import com.google.api.services.books.model.Volume;
import com.google.api.services.books.model.Volumes;

import java.util.List;

public final class Utils {

    private Utils() {

    }

    public static List<Volume> pesquisarLivros(JsonFactory jsonFactory, String pesquisa) throws Exception {
        final Books books = new Books.Builder(new ApacheHttpTransport(), jsonFactory, null)
                .setApplicationName("Bookshelf-AndroidVIP")
                .setGoogleClientRequestInitializer(new BooksRequestInitializer(K.API_KEY))
                .build();

        Books.Volumes.List listaVolumes = books.volumes().list(pesquisa);
        listaVolumes.setMaxResults(40L);
        Volumes volumes = listaVolumes.execute();

        return volumes.getItems();
    }

    public static boolean temConexao() {
        // TODO: 30/01/2018  
        return true;
    }
}
