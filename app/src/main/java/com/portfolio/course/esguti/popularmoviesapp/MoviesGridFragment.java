package com.portfolio.course.esguti.popularmoviesapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;


/**
 * A fragment containing the grid view of Android versions.
 */
public class MoviesGridFragment extends Fragment {

    private final String LOG_TAG = MoviesGridFragment.class.getSimpleName();
    static final MoviesItemAdapter STATE_ADAPTER = null;
    private MoviesItemAdapter m_movieAdapter;
    private int m_numPage = 1;

    public MoviesGridFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_movies_grid, container, false);

        //create the adaptor
        m_movieAdapter = new MoviesItemAdapter(getActivity());

        // Get a reference to the GridView, and attach this adapter to it.
        final GridView gridView = (GridView) rootView.findViewById(R.id.fragment_movies_grid);
        gridView.setAdapter(m_movieAdapter);

        // in debug mode show indicators
        if (BuildConfig.DEBUG){ Picasso.with(getContext()).setIndicatorsEnabled(true); }

        //add the listener for launch detail when movie is pressed
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MovieItem movie = m_movieAdapter.getItem(position);
                Intent movieIntent = new Intent(getActivity(), MovieDetailActivity.class);
                movieIntent.putExtra(MovieItem.class.getName(), movie);

                // Verify that the intent will resolve to an activity
                if (movieIntent.resolveActivity(getContext().getPackageManager()) != null) {
                    startActivity(movieIntent);
                }
            }
        });

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_movies, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id){
            case R.id.action_settings:
                Intent settings = new Intent(getActivity(), SettingsActivity.class);
                startActivity(settings);
                return true;
            case R.id.action_refresh:
                updateMovies();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart(){
        super.onStart();
        updateMovies();
    }

    private void updateMovies(){
        FetchMoviesTask moviesTask = new FetchMoviesTask();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sort_mode = prefs.getString(getString(R.string.pref_header_sort_key),
                getString(R.string.pref_header_sort_key_default));
        moviesTask.execute(sort_mode);
    }

    /**
     * Background task to fetch movie info from themoviedb API.
     */
    public class FetchMoviesTask extends AsyncTask<String, Void, ArrayList<MovieItem>> {

        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        @Override
        protected ArrayList<MovieItem> doInBackground(String... params) {

            // If there's no sort filter, just look for all movies.
            String sort_param = getString(R.string.pref_header_sort_key_default);
            if (params.length == 1) { sort_param = params[0]; }


            // Check if the NetworkConnection is active and connected.
            ConnectivityManager connMgr = (ConnectivityManager)
                    getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo == null || !networkInfo.isConnected()) {
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String movieJsonStr = null;

            try {

                Resources res = getResources();
                Uri builtUri = Uri.parse(res.getString(R.string.tmdb_base_url)).buildUpon()
                        .appendQueryParameter(
                                res.getString(R.string.tmdb_param_key),
                                res.getString(R.string.MOVIEDB_API_KEY))
                        .appendQueryParameter(
                                res.getString(R.string.tmdb_param_sort), sort_param)
                        .appendQueryParameter(
                                res.getString(R.string.tmdb_param_page), Integer.toString(m_numPage))
                                .build();
                URL url = new URL(builtUri.toString());

                Log.d(LOG_TAG, "Built URI " + builtUri.toString());

                // Create the request to themoviedb, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(10000 /* milliseconds */);
                urlConnection.setConnectTimeout(15000 /* milliseconds */);
                urlConnection.addRequestProperty("Accept", "application/json");
                urlConnection.setDoInput(true);
                urlConnection.connect();

                Log.d(LOG_TAG, "Response: "
                        + urlConnection.getResponseCode() + " "
                        + urlConnection.getResponseMessage());

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) { return null; }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                // append new line for debugging
                String line;
                while ((line = reader.readLine()) != null) { buffer.append(line + "\n"); }

                if (buffer.length() == 0) { return null; }
                movieJsonStr = buffer.toString();

                Log.d(LOG_TAG, "TMDB string: " + buffer);

            } catch (MalformedURLException e) {
                Log.e(LOG_TAG, "Malformed URL ", e);
            } catch (ProtocolException e) {
                Log.e(LOG_TAG, "Protocol Error ", e);
            } catch (IOException e) {
                Log.e(LOG_TAG, "IO Error ", e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return parseResult(movieJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error parsing JSON String. " + e.getMessage(), e);
            }

            return null;

        } // end of background


        @Override
        protected void onPostExecute(ArrayList<MovieItem> result) {
            if (result != null) {
                m_movieAdapter.clear();
                m_movieAdapter.addAll(result);
            }
        }

    } // end of AsyncTask


    private ArrayList<MovieItem> parseResult(String result) throws JSONException{
        ArrayList<MovieItem> results = new ArrayList<MovieItem>();

        JSONObject jsonObject = new JSONObject(result);
        JSONArray array = (JSONArray) jsonObject.get("results");
        for (int i = 0; i < array.length(); i++) {
            JSONObject jsonMovieObject = array.getJSONObject(i);

            if( jsonMovieObject.has(getString(R.string.tmdb_param_id))
                    && jsonMovieObject.has(getString(R.string.tmdb_param_title))) {
                MovieItem movieItem = new MovieItem(
                        Integer.parseInt(jsonMovieObject.getString(getString(R.string.tmdb_param_id))),
                        jsonMovieObject.getString(getString(R.string.tmdb_param_title))
                        );
                if( jsonMovieObject.has(getString(R.string.tmdb_param_originalTitle)) )
                    movieItem.setOriginalTitle(jsonMovieObject.getString(getString(R.string.tmdb_param_originalTitle)));
                if( jsonMovieObject.has(getString(R.string.tmdb_param_synopsis)) )
                    movieItem.setSynopsis(jsonMovieObject.getString(getString(R.string.tmdb_param_synopsis)));
                if( jsonMovieObject.has(getString(R.string.tmdb_param_popularity)) )
                    movieItem.setPopularity(jsonMovieObject.getString(getString(R.string.tmdb_param_popularity)));
                if( jsonMovieObject.has(getString(R.string.tmdb_param_totalVotes)) )
                    movieItem.setTotalVotes(jsonMovieObject.getString(getString(R.string.tmdb_param_totalVotes)));
                if( jsonMovieObject.has(getString(R.string.tmdb_param_releaseDate)) )
                    movieItem.setReleaseDate(jsonMovieObject.getString(getString(R.string.tmdb_param_releaseDate)));
                if( jsonMovieObject.has(getString(R.string.tmdb_param_poster_thumb)) )
                    movieItem.setPosterThumb(jsonMovieObject.getString(getString(R.string.tmdb_param_poster_thumb)));
                if( jsonMovieObject.has(getString(R.string.tmdb_param_posterPath)) )
                    movieItem.setPosterPath(jsonMovieObject.getString(getString(R.string.tmdb_param_posterPath)));

                results.add(movieItem);
            }
        }

        return results;
    } // end of parseResult

} // end of MoviesGridFragment class