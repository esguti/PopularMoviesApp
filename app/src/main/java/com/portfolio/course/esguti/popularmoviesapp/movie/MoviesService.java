package com.portfolio.course.esguti.popularmoviesapp.movie;

/**
 * Created by esguti on 15.01.16.
 */

import com.portfolio.course.esguti.popularmoviesapp.MovieItem;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MoviesService {


    /**
     * Get the basic movie information for a specific movie id.
     *
     * @param tmdbId TMDb id.
     * @param language <em>Optional.</em> ISO 639-1 code.
     */
    @GET("3/movie/{id}")
    Call<MovieItem> summary(
            @Path("id") int tmdbId,
            @Query("language") String language
    );

    /**
     * Get the reviews for a particular movie id.
     *
     * @param tmdbId   TMDb id.
     * @param page     <em>Optional.</em> Minimum value is 1, expected value is an integer.
     * @param language <em>Optional.</em> ISO 639-1 code.
     */
    @GET("/3/movie/{id}/reviews")
    Call<Reviews> reviews(
            @Path("id") int tmdbId,
            @Query("page") Integer page,
            @Query("language") String language
    );

    /**
     * Get the videos (trailers, teasers, clips, etc...) for a specific movie id.
     *
     * @param tmdbId   TMDb id.
     * @param language <em>Optional.</em> ISO 639-1 code.
     */
    @GET("/3/movie/{id}/videos")
    Call<Trailers> trailers(
            @Path("id") int tmdbId,
            @Query("language") String language
    );

}
