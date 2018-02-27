package com.androidvip.bookshelf.model;

import android.support.annotation.IntDef;

import java.util.Date;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class Book {
    @Id
    private long id;
    private String title;
    private String authors;
    private int score;
    private String tags;
    private boolean favorite;
    private String googleBooksId;
    private Date readingStartDate;
    private Date readingEndDate;
    private int readingState;

    @IntDef({STATE_READING, STATE_WISH, STATE_ON_HOLD, STATE_DROPPED, STATE_FINISHED})
    private @interface ReadingState {}

    public static final int STATE_READING = 1;
    public static final int STATE_WISH = 2;
    public static final int STATE_ON_HOLD = 3;
    public static final int STATE_DROPPED = 4;
    public static final int STATE_FINISHED = 5;

    public Book() {

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public String getGoogleBooksId() {
        return googleBooksId;
    }

    public void setGoogleBooksId(String googleBooksId) {
        this.googleBooksId = googleBooksId;
    }

    public Date getReadingStartDate() {
        return readingStartDate;
    }

    public void setReadingStartDate(Date readingStartDate) {
        this.readingStartDate = readingStartDate;
    }

    public Date getReadingEndDate() {
        return readingEndDate;
    }

    public void setReadingEndDate(Date readingEndDate) {
        this.readingEndDate = readingEndDate;
    }

    public int getReadingState() {
        return readingState;
    }

    public void setReadingState(@ReadingState int readingState) {
        this.readingState = readingState;
    }

    @Override
    public String toString() {
        return title + " - " + authors;
    }
}
