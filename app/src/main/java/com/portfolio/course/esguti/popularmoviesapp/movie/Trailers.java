package com.portfolio.course.esguti.popularmoviesapp.movie;

/**
 * Created by esguti on 15.01.16.
 */

import java.util.List;

public class Trailers {

    public static class Trailer {

        public String id;
        public String iso_639_1;
        public String key;
        public String name;
        public String site;
        public Integer size;
        public String type;

    }

    public Integer id;
    public List<Trailer> results;

}
