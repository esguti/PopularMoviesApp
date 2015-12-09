package com.portfolio.course.esguti.popularmoviesapp;

/**
 * Created by esguti on 04.12.15.
 */
public class MovieItem {
    Integer id;
    String  title = "";
    String  poster_path = "";

    public MovieItem (Integer id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPoster_path(String poster_path) {
        this.poster_path = poster_path;
    }
}
