package com.portfolio.course.esguti.popularmoviesapp;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by esguti on 04.12.15.
 */
public class MovieItem implements Parcelable {
    private Integer id;
    private String  title = "";
    private String  originalTitle = null;
    private String  synopsis = null;
    private String  popularity = null;
    private String  releaseDate = null;
    private String  posterThumbail = null;
    private String  posterPath = null;

    // CONSTRUCTORS

    public MovieItem (Integer id, String title) {
        this.id    = id;
        this.title = title;
    }

    protected MovieItem(Parcel in) {
        id             = in.readInt();
        title          = in.readString();
        originalTitle  = in.readString();
        synopsis       = in.readString();
        popularity     = in.readString();
        releaseDate    = in.readString();
        posterThumbail = in.readString();
        posterPath     = in.readString();
    }


    //GET methods

    public Integer getId()            { return id;    }
    public String  getTitle()         { return title; }
    public String  getOriginalTitle() { return originalTitle; }
    public String  getSynopsis()      { return synopsis; }
    public String  getPopularity()    { return popularity; }
    public String  getReleaseDate()   { return releaseDate; }
    public String  getPosterThumbail(Context context) {
        if (posterPath == null)
            return ContentResolver.SCHEME_ANDROID_RESOURCE +
                    "://" + context.getResources().getResourcePackageName(R.drawable.no_image)
                    + '/' + context.getResources().getResourceTypeName(R.drawable.no_image)
                    + '/' + context.getResources().getResourceEntryName(R.drawable.no_image);
        else return context.getString(R.string.tmdb_poster_base_url) + "/" + posterPath;
    }
    public String getPosterPath(Context context) {
        if (posterThumbail == null)
            return ContentResolver.SCHEME_ANDROID_RESOURCE +
                    "://" + context.getResources().getResourcePackageName(R.drawable.thumbail_background)
                    + '/' + context.getResources().getResourceTypeName(R.drawable.thumbail_background)
                    + '/' + context.getResources().getResourceEntryName(R.drawable.thumbail_background);
        else return context.getString(R.string.tmdb_poster_base_url) + "/" + posterPath;
    }

    //SET methods
    public void setOriginalTitle(String popularity) { this.popularity = popularity; }
    public void setSynopsis(String synopsis) { this.synopsis = synopsis; }
    public void setPopularity(String popularity) { this.popularity = popularity; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }
    public void setPosterThumbail(String posterThumbail) { this.posterThumbail = posterThumbail; }
    public void setPosterPath(String posterPath) { this.posterPath = posterPath; }


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
        dest.writeString(originalTitle);
        dest.writeString(synopsis);
        dest.writeString(popularity);
        dest.writeString(releaseDate);
        dest.writeString(posterThumbail);
        dest.writeString(posterPath);
    }
}
