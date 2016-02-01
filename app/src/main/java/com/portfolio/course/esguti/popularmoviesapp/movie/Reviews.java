package com.portfolio.course.esguti.popularmoviesapp.movie;

import java.util.List;

/**
 * Created by esguti on 15.01.16.
 */

public class Reviews {

    public static class Review {

        public String id;
        public String author;
        public String content;
        public String url;

    }

    public Integer page;
    public Integer total_pages;
    public Integer total_results;
    public Integer id;
    public List<Review> results;
    public String url;

}
