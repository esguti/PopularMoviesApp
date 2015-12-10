package com.portfolio.course.esguti.popularmoviesapp;

import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Toast;

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
import java.util.Arrays;


/**
 * A fragment containing the grid view of Android versions.
 */
public class MoviesGridFragment extends Fragment {

    private final String LOG_TAG = MoviesGridFragment.class.getSimpleName();
    private MoviesItemAdapter g_movieAdapter;

    public MoviesGridFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_movies_grid, container, false);

        //create the adaptor
        g_movieAdapter = new MoviesItemAdapter(getActivity());
        // Get a reference to the GridView, and attach this adapter to it.
        GridView gridView = (GridView) rootView.findViewById(R.id.fragment_movies_grid);
        gridView.setAdapter(g_movieAdapter);
        // in debug mode show indicators
        if (BuildConfig.DEBUG){ Picasso.with(getContext()).setIndicatorsEnabled(true); }
        FetchMoviesTask moviesTask = new FetchMoviesTask();
        moviesTask.execute();
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
        switch (item.getItemId()) {
            case R.id.sort_highest_rated:
                FetchMoviesTask moviesTask1 = new FetchMoviesTask();
                moviesTask1.execute("vote_average.desc");
                return true;

            case R.id.sort_most_popular:
                FetchMoviesTask moviesTask2 = new FetchMoviesTask();
                moviesTask2.execute("popularity.desc");
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Background task to fetch movie info from themoviedb API.
     */
    public class FetchMoviesTask extends AsyncTask<String, Void, ArrayList<MovieItem>> {

        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        @Override
        protected ArrayList<MovieItem> doInBackground(String... params) {

            // If there's no sort filter, just look for all films.
            String sort_param = "";
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
                                res.getString(R.string.tmdb_key_param),
                                res.getString(R.string.MOVIEDB_API_KEY))
                        .appendQueryParameter(
                                res.getString(R.string.tmdb_sort_param), sort_param)
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
                g_movieAdapter.clear();
                g_movieAdapter.addAll(result);
            }
        }

    } // end of AsyncTask


    private ArrayList<MovieItem> parseResult(String result) throws JSONException{
        ArrayList<MovieItem> results = new ArrayList<MovieItem>();

        JSONObject jsonObject = new JSONObject(result);
        JSONArray array = (JSONArray) jsonObject.get("results");
        for (int i = 0; i < array.length(); i++) {
            JSONObject jsonMovieObject = array.getJSONObject(i);

            if( jsonMovieObject.has("title") && jsonMovieObject.has("id")) {
                MovieItem movieItem = new MovieItem(
                        Integer.parseInt(jsonMovieObject.getString("id")),
                        jsonMovieObject.getString("title")
                        );

                if( jsonMovieObject.has("poster_path") )
                    movieItem.setPoster_path(jsonMovieObject.getString("poster_path"));

                results.add(movieItem);
            }
        }

        return results;
    } // end of parseResult

} // end of MoviesGridFragment class