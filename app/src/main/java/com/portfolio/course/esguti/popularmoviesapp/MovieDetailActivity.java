package com.portfolio.course.esguti.popularmoviesapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MovieDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new MovieDetailActivityFragment())
                    .commit();
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class MovieDetailActivityFragment extends android.support.v4.app.Fragment {

        public MovieDetailActivityFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);

            // obtain Parcelable Object
            MovieItem movie = getActivity().getIntent().getParcelableExtra(MovieItem.class.getName());

            //Change activity name
            getActivity().setTitle(movie.getTitle());

            //get layout fields
            ImageView img_thumb = (ImageView) rootView.findViewById(R.id.movie_detail_thumb_img);
            TextView  txt_original_title = (TextView) rootView.findViewById(R.id.movie_detail_thumb_title);
            TextView  txt_release_date = (TextView) rootView.findViewById(R.id.movie_detail_thumb_date);
            RatingBar rbar_popularity = (RatingBar) rootView.findViewById(R.id.movie_detail_popbar);
            TextView  txt_popularity = (TextView) rootView.findViewById(R.id.movie_detail_popularity_text);
            TextView  txt_synopsis = (TextView) rootView.findViewById(R.id.movie_detail_synopsis);

            // fulfill layout fields
            Picasso.with(getContext()).load(movie.getPosterThumb(getContext())).resize(185, 185).into(img_thumb);
            txt_original_title.setText(movie.getOriginalTitle());
            String release_date = movie.getReleaseDate();
            SimpleDateFormat src_date_format = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat dst_date_format = new SimpleDateFormat("dd MMM yyyy");
            try {
                Date date = src_date_format.parse(release_date);
                txt_release_date.setText( dst_date_format.format(date));
            } catch (ParseException e) {
                txt_release_date.setText("no available");
            }
            String starNum = getString(R.string.movie_detail_popbar_star_num);
            String popularity = movie.getPopularity();
            String totalvotes = movie.getTotalVotes();
            float popularity_star = Float.parseFloat(popularity) * Float.parseFloat(starNum) / 10;
            txt_popularity.setText(
                    String.format("%.1f/%s (%s votes)", popularity_star, starNum, totalvotes));
            rbar_popularity.setRating(popularity_star);
            txt_synopsis.setText(movie.getSynopsis());

            return rootView;
        }
    }
}
