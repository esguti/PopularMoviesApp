package com.portfolio.course.esguti.popularmoviesapp;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by esguti on 04.12.15.
 */
public class MovieItem implements Parcelable {
    public Integer id;
    public String title = "";
    public String original_title= null;
    public String overview = null;
    public String popularity = "0";
    public String vote_count = "0";
    public String release_date = null;
    public String backdrop_path = null;
    public String poster_path = null;

    // CONSTRUCTORS

    public MovieItem(Integer id, String title) {
        this.id = id;
        this.title = title;
    }

    protected MovieItem(Parcel in) {
        id = in.readInt();
        title = in.readString();
        original_title = in.readString();
        overview = in.readString();
        popularity = in.readString();
        vote_count = in.readString();
        release_date = in.readString();
        backdrop_path = in.readString();
        poster_path = in.readString();
    }


    //GET methods

    public Integer getId()            { return id;    }
    public String  getTitle()         { return title; }
    public String  getOriginalTitle() { return original_title; }
    public String  getOverview()      { return overview; }
    public String  getPopularity()    { return popularity; }
    public String  getVote_count()    { return vote_count; }
    public String  getRelease_date()   { return release_date; }
    public String  getPosterThumb(Context context) {
        if (backdrop_path == null)
            return ContentResolver.SCHEME_ANDROID_RESOURCE +
                    "://" + context.getResources().getResourcePackageName(R.drawable.thumb_background)
                    + '/' + context.getResources().getResourceTypeName(R.drawable.thumb_background)
                    + '/' + context.getResources().getResourceEntryName(R.drawable.thumb_background);
        else return context.getString(R.string.tmdb_poster_base_url) + "/" + backdrop_path;
    }
    public String getPosterPath(Context context) {
        if (poster_path == null)
            return ContentResolver.SCHEME_ANDROID_RESOURCE +
                    "://" + context.getResources().getResourcePackageName(R.drawable.no_image)
                    + '/' + context.getResources().getResourceTypeName(R.drawable.no_image)
                    + '/' + context.getResources().getResourceEntryName(R.drawable.no_image);
        else return context.getString(R.string.tmdb_poster_base_url) + "/" + poster_path;
    }

    //SET methods
    public void setOriginalTitle(String originalTitle){ this.original_title = originalTitle; }
    public void setOverview(String overview) { this.overview = overview; }
    public void setPopularity(String popularity) {
        if( popularity != null ) this.popularity = popularity; }
    public void setVote_count(String vote_count) {
        if( vote_count != null ) this.vote_count = vote_count; }
    public void setRelease_date(String release_date) { this.release_date = release_date; }
    public void setBackdrop_path(String backdrop_path) {
        if( backdrop_path != "null" ){ this.backdrop_path = backdrop_path; } }
    public void setPoster_path(String poster_path) {
        if( poster_path != "null" ){ this.poster_path = poster_path; } }


    //PARCEABLE methods

    public static final Creator<MovieItem> CREATOR = new Creator<MovieItem>() {
        @Override
        public MovieItem createFromParcel(Parcel in) {
            return new MovieItem(in);
        }

        @Override
        public MovieItem[] newArray(int size) {
            return new MovieItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(original_title);
        dest.writeString(overview);
        dest.writeString(popularity);
        dest.writeString(vote_count);
        dest.writeString(release_date);
        dest.writeString(backdrop_path);
        dest.writeString(poster_path);
    }
}
