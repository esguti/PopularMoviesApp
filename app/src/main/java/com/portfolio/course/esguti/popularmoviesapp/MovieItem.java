package com.portfolio.course.esguti.popularmoviesapp;

import android.content.ContentResolver;
import android.content.Context;

/**
 * Created by esguti on 04.12.15.
 */
public class MovieItem {
    private Integer id;
    private String  title = "";
    private String  poster_path = null;

    // Constructor
    public MovieItem (Integer id, String title) {
        this.id    = id;
        this.title = title;
    }

    //GET methods
    public Integer getId()    { return id;    }
    public String  getTitle() { return title; }
    public String  getPoster_path(Context context) {
        if (poster_path == null)
            return ContentResolver.SCHEME_ANDROID_RESOURCE +
                    "://" + context.getResources().getResourcePackageName(R.drawable.no_image)
                    + '/' + context.getResources().getResourceTypeName(R.drawable.no_image)
                    + '/' + context.getResources().getResourceEntryName(R.drawable.no_image);
        else return context.getString(R.string.tmdb_poster_base_url) + "/" + poster_path;
    }

    //SET methods
    public void setPoster_path(String poster_path) {
        if(poster_path != "null") this.poster_path = poster_path;
    }
}
