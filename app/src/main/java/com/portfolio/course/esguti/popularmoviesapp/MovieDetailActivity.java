package com.portfolio.course.esguti.popularmoviesapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.portfolio.course.esguti.popularmoviesapp.movie.MoviesService;
import com.portfolio.course.esguti.popularmoviesapp.movie.MoviesServiceGenerator;
import com.portfolio.course.esguti.popularmoviesapp.movie.Reviews;
import com.portfolio.course.esguti.popularmoviesapp.movie.Trailers;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MovieDetailActivity extends AppCompatActivity {

    private final String LOG_TAG = MovieDetailActivity.class.getSimpleName();

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


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void clickFavorite(View view) {
        String movie_id = String.valueOf(view.getTag());
        ImageView img= (ImageView) view.findViewById(R.id.movie_detail_favorite_img);

        if( Favorites.existFavorite(this, movie_id) ){
            // delete and show unmarked
            Favorites.delFavoriteItem(this, movie_id);
            img.setImageResource(android.R.drawable.btn_star_big_off);
        }else{
            //add and show marked
            Favorites.addFavoriteItem(this, movie_id);
            img.setImageResource(android.R.drawable.btn_star_big_on);
        }
    }

    public void openVideo(View view) {
        //Get url from tag
        String id = (String) view.getTag();

        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://www.youtube.com/watch?v=" + id));
        startActivity(intent);
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class MovieDetailActivityFragment extends android.support.v4.app.Fragment {

        private final String LOG_TAG = MovieDetailActivityFragment.class.getSimpleName();

        private MoviesReviewAdapter m_adapterReviews;
        private MoviesTrailerAdapter m_adapterTrailers;
        private RecyclerView m_viewReviews, m_viewTrailers;
        private LinearLayoutManager m_layoutReviews, m_layoutTrailers;

        public MovieDetailActivityFragment() {
        }

        void insertReviews(View rootView, MovieItem movie, MoviesService client) {
            m_viewReviews = (RecyclerView) rootView.findViewById(R.id.fragment_movie_detail_reviews);
            m_layoutReviews = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
            m_adapterReviews = new MoviesReviewAdapter(getActivity());
            m_viewReviews.setAdapter(m_adapterReviews);
            m_viewReviews.setLayoutManager(m_layoutReviews);
            m_viewReviews.setNestedScrollingEnabled(true);

            Call<Reviews> call = client.reviews(movie.getId(), null, null);
            call.enqueue(new Callback<Reviews>() {
                @Override
                public void onResponse(Response<Reviews> response) {
                    Log.d(LOG_TAG, "Status Code = " + response.code());
                    if (response.isSuccess()) {
                        // request successful (status code 200, 201)
                        Reviews reviewsList = response.body();
                        Log.d(LOG_TAG, "Reviews = " + reviewsList.results.size());

                        m_adapterReviews.setReviewList(reviewsList.results);

                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    Toast.makeText(getContext(), "Get Reviews Failed", Toast.LENGTH_LONG)
                            .show();
                    Log.d(LOG_TAG, "Get Reviews Failed: " + t.toString());
                }
            });
        }


        void insertTrailers(View rootView, MovieItem movie, MoviesService client) {
            m_viewTrailers = (RecyclerView) rootView.findViewById(R.id.fragment_movie_detail_trailers);
            m_layoutTrailers = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
            m_adapterTrailers = new MoviesTrailerAdapter(getActivity());
            m_viewTrailers.setAdapter(m_adapterTrailers);
            m_viewTrailers.setLayoutManager(m_layoutTrailers);
            m_viewTrailers.setNestedScrollingEnabled(true);

            Call<Trailers> call = client.trailers(movie.getId(), null);
            call.enqueue(new Callback<Trailers>() {
                @Override
                public void onResponse(Response<Trailers> response) {
                    Log.d(LOG_TAG, "Status Code = " + response.code());
                    if (response.isSuccess()) {
                        // request successful (status code 200, 201)
                        Trailers trailersList = response.body();
                        Log.d(LOG_TAG, "Trailers = " + trailersList.results.size());

                        m_adapterTrailers.setTrailerList(trailersList.results);

                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    Toast.makeText(getContext(), "Get Trailer Failed", Toast.LENGTH_LONG)
                            .show();
                    Log.d(LOG_TAG, "Get Trailer Failed: " + t.toString());
                }
            });
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);


            // obtain Parcelable Object
            MovieItem movie = getActivity().getIntent().getParcelableExtra(MovieItem.class.getName());

            MoviesService client = MoviesServiceGenerator.createService(MoviesService.class, rootView.getContext());

            insertReviews(rootView, movie, client);
            insertTrailers(rootView, movie, client);

            //Change activity name
            getActivity().setTitle(movie.getTitle());

            //get layout fields
            ImageView img_thumb = (ImageView) rootView.findViewById(R.id.movie_detail_thumb_img);
            ImageView img_fav = (ImageView) rootView.findViewById(R.id.movie_detail_favorite_img);
            TextView txt_original_title = (TextView) rootView.findViewById(R.id.movie_detail_thumb_title);
            TextView txt_release_date = (TextView) rootView.findViewById(R.id.movie_detail_thumb_date);
            RatingBar rbar_popularity = (RatingBar) rootView.findViewById(R.id.movie_detail_popbar);
            TextView txt_popularity = (TextView) rootView.findViewById(R.id.movie_detail_popularity_text);
            TextView txt_synopsis = (TextView) rootView.findViewById(R.id.movie_detail_synopsis);

            // fullfil favorite anf get from db
            img_fav.setTag(movie.getId());
            String movie_key = String.valueOf(movie.getId());
            Log.d(LOG_TAG,movie_key);
            if( Favorites.existFavorite(getActivity(), movie_key) )
                img_fav.setImageResource(android.R.drawable.btn_star_big_on);
            else
                img_fav.setImageResource(android.R.drawable.btn_star_big_off);

            // fulfill layout fields
            Picasso.with(getContext()).load(movie.getPosterThumb(getContext())).resize(185, 185).into(img_thumb);
            txt_original_title.setText(movie.getOriginalTitle());
            String release_date = movie.getRelease_date();
            SimpleDateFormat src_date_format = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat dst_date_format = new SimpleDateFormat("dd MMM yyyy");
            try {
                Date date = src_date_format.parse(release_date);
                txt_release_date.setText(dst_date_format.format(date));
            } catch (ParseException e) {
                txt_release_date.setText("no available");
            }
            String starNum = getString(R.string.movie_detail_popbar_star_num);
            String popularity = movie.getPopularity();
            String totalvotes = movie.getVote_count();
            float popularity_star = Float.parseFloat(popularity) * Float.parseFloat(starNum) / 10;
            txt_popularity.setText(
                    String.format("%.1f/%s (%s votes)", popularity_star, starNum, totalvotes));
            rbar_popularity.setRating(popularity_star);
            txt_synopsis.setText(movie.getOverview());

            return rootView;
        }
    }
}
