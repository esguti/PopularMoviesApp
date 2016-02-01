package com.portfolio.course.esguti.popularmoviesapp;

import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

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
 * Background task to fetch movie info from themoviedb API.
 */
public class FetchMoviesTask extends AsyncTask<String, Void, ArrayList<MovieItem>> {

    private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();
    private Context m_context;
    private MoviesGridFragment m_moviesGridFragment;

    public FetchMoviesTask(Context context, MoviesGridFragment moviesGridFragment) {
        m_context = context;
        m_moviesGridFragment = moviesGridFragment;
    }

    @Override
    protected ArrayList<MovieItem> doInBackground(String... params) {

        // If there's no sort filter, just look for all movies.
        String sort_param = m_context.getResources().getString(R.string.pref_header_sort_key_default);
        String num_page = "1";
        if (params.length > 0) { sort_param = params[0]; }
        if (params.length > 1) { num_page = params[1]; }

        // Check if the NetworkConnection is active and connected.
        ConnectivityManager connMgr = (ConnectivityManager)
                m_context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            return null;
        }

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String movieJsonStr = null;

        try {

            Resources res = m_context.getResources();
            Uri builtUri = Uri.parse(res.getString(R.string.tmdb_base_url)).buildUpon()
                    .appendQueryParameter(
                            res.getString(R.string.tmdb_param_key),
                            res.getString(R.string.MOVIEDB_API_KEY))
                    .appendQueryParameter(
                            res.getString(R.string.tmdb_param_sort), sort_param)
                    .appendQueryParameter(
                            res.getString(R.string.tmdb_param_page), num_page)
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

            return parseResult(movieJsonStr);

        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Malformed URL ", e);
        } catch (ProtocolException e) {
            Log.e(LOG_TAG, "Protocol Error ", e);
        } catch (IOException e) {
            Log.e(LOG_TAG, "IO Error ", e);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error parsing JSON String. " + e.getMessage(), e);
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

        return null;

    } // end of background


    @Override
    protected void onPostExecute(ArrayList<MovieItem> result) {
        m_moviesGridFragment.postupdateMovies(result);
    }

    private ArrayList<MovieItem> parseResult(String result) throws JSONException {
        ArrayList<MovieItem> results = new ArrayList<MovieItem>();

        Resources res = m_context.getResources();
        JSONObject jsonObject = new JSONObject(result);
        JSONArray array = (JSONArray) jsonObject.get("results");
        for (int i = 0; i < array.length(); i++) {
            JSONObject jsonMovieObject = array.getJSONObject(i);

            if (jsonMovieObject.has(res.getString(R.string.tmdb_param_id))
                    && jsonMovieObject.has(res.getString(R.string.tmdb_param_title))) {
                MovieItem movieItem = new MovieItem(
                        Integer.parseInt(jsonMovieObject.getString(res.getString(R.string.tmdb_param_id))),
                        jsonMovieObject.getString(res.getString(R.string.tmdb_param_title))
                );
                if (jsonMovieObject.has(res.getString(R.string.tmdb_param_originalTitle)))
                    movieItem.setOriginalTitle(jsonMovieObject.getString(res.getString(R.string.tmdb_param_originalTitle)));
                if (jsonMovieObject.has(res.getString(R.string.tmdb_param_synopsis)))
                    movieItem.setSynopsis(jsonMovieObject.getString(res.getString(R.string.tmdb_param_synopsis)));
                if (jsonMovieObject.has(res.getString(R.string.tmdb_param_popularity)))
                    movieItem.setPopularity(jsonMovieObject.getString(res.getString(R.string.tmdb_param_popularity)));
                if (jsonMovieObject.has(res.getString(R.string.tmdb_param_totalVotes)))
                    movieItem.setTotalVotes(jsonMovieObject.getString(res.getString(R.string.tmdb_param_totalVotes)));
                if (jsonMovieObject.has(res.getString(R.string.tmdb_param_releaseDate)))
                    movieItem.setReleaseDate(jsonMovieObject.getString(res.getString(R.string.tmdb_param_releaseDate)));
                if (jsonMovieObject.has(res.getString(R.string.tmdb_param_poster_thumb)))
                    movieItem.setPosterThumb(jsonMovieObject.getString(res.getString(R.string.tmdb_param_poster_thumb)));
                if (jsonMovieObject.has(res.getString(R.string.tmdb_param_posterPath)))
                    movieItem.setPosterPath(jsonMovieObject.getString(res.getString(R.string.tmdb_param_posterPath)));

                results.add(movieItem);
            }
        }

        return results;
    } // end of parseResult


} // end of AsyncTask

