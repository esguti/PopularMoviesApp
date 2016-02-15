package com.portfolio.course.esguti.popularmoviesapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;

import com.portfolio.course.esguti.popularmoviesapp.movie.MoviesService;
import com.portfolio.course.esguti.popularmoviesapp.movie.MoviesServiceGenerator;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * A fragment containing the grid view of Android versions.
 */
public class MoviesGridFragment extends Fragment {

    private static final String LOG_TAG = MoviesGridFragment.class.getSimpleName();

    private static final String CURR_POS  = "currentPos";
    private static final String LAST_PAGE = "m_lastPage";
    private static final String CURR_PAGE = "m_currentPage";
    private static final String PREV_TOT  = "m_previousTotal";
    private static final String PREV_SORT = "m_previous_sort_mode";
    private static final String MOV_ITEMS = "m_movieItems";
    
    private MoviesItemAdapter m_movieAdapter;
    private GridView m_gridView;
    private static final int MAX_PAGE = 1000;
    private int VISIBLE_THRESHOLD = 5;
    private boolean m_isLoading = false;
    private boolean m_lastPage = false;
    private int m_currentPage = 1;
    private int m_previousTotal = 0;
    private String m_previous_sort_mode = null;

    public MoviesGridFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    // Determines if this is a one or two pane layout
    private static boolean isTwoPanel(Activity act) {
        FrameLayout frm = (FrameLayout) act.findViewById(R.id.container);
        int heithDph = act.getResources().getConfiguration().screenHeightDp;
        int widthDph = act.getResources().getConfiguration().screenWidthDp;
        Log.d(LOG_TAG,
                "Width:" + String.valueOf(widthDph) + " Height:" + String.valueOf(heithDph));
        if( frm != null){
            return true;
        }else{
            return false;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_movies_grid, container, false);

        //create the adaptor
        m_movieAdapter = new MoviesItemAdapter(getActivity());

        // Get a reference to the GridView, and attach this adapter to it.
        m_gridView = (GridView) rootView.findViewById(R.id.fragment_movies_grid);
        m_gridView.setAdapter(m_movieAdapter);

        // in debug mode show indicators
        if (BuildConfig.DEBUG){ Picasso.with(getContext()).setIndicatorsEnabled(true); }

        //add the listener for launch detail when movie is pressed
        m_gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                MovieItem movie = m_movieAdapter.getItem(position);

                if ( isTwoPanel(getActivity()) ) { // single activity with list and detail

                    // Replace framelayout with new detail fragment
                    MovieDetailActivity.MovieDetailActivityFragment fragmentItem = MovieDetailActivity.newInstance(movie);
                    android.support.v4.app.FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.container, fragmentItem);
                    ft.commit();
                } else { // go to separate activity

                    Intent movieIntent = new Intent(getActivity(), MovieDetailActivity.class);
                    movieIntent.putExtra(MovieItem.class.getName(), movie);

                    // Verify that the intent will resolve to an activity
                    if (movieIntent.resolveActivity(getContext().getPackageManager()) != null) {
                        startActivity(movieIntent);
                    }
                }
            }
        });

        //add the listener for loading more movies when end of list is reached
        m_gridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (m_isLoading) {
                    if (totalItemCount > m_previousTotal) {
                        m_previousTotal = totalItemCount;
                    }
                }

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                String sort_mode = prefs.getString(getString(R.string.pref_header_sort_key),
                        getString(R.string.pref_header_sort_key_default));
                String fav_key = getString(R.string.pref_header_sort_key_favorite);

                if (!m_lastPage && !m_isLoading && !sort_mode.equals(fav_key)
                        && (totalItemCount - VISIBLE_THRESHOLD) <= (firstVisibleItem + visibleItemCount)) {
                    updateMovies(m_currentPage);
                }
            }
        });

        String clean = "null";
        if (savedInstanceState != null) clean = savedInstanceState.toString();
        Log.d(LOG_TAG, "Call onCreateView: " + clean);
        reloadInstance(savedInstanceState);

        return rootView;
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        saveInstance(savedInstanceState);
    }


    @Override
    public void onResume() {
        super.onResume();

        //reset in case of sort mode change
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String currrent_sort_mode = prefs.getString(getString(R.string.pref_header_sort_key),
                getString(R.string.pref_header_sort_key_default));
        if (m_previous_sort_mode != null && !m_previous_sort_mode.equals(currrent_sort_mode)) {
            resetPage();
        }
    }


    private void saveInstance(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        savedInstanceState.putInt(CURR_POS, m_gridView.getFirstVisiblePosition());
        savedInstanceState.putBoolean(LAST_PAGE, m_lastPage);
        savedInstanceState.putInt(CURR_PAGE, m_currentPage);
        savedInstanceState.putInt(PREV_TOT, m_previousTotal);
        savedInstanceState.putString(PREV_SORT, m_previous_sort_mode);
        ArrayList<MovieItem> movieItems = new ArrayList<>();
        for (int i = 0; i < m_movieAdapter.getCount(); i++) {
            movieItems.add(m_movieAdapter.getItem(i));
        }
        savedInstanceState.putParcelableArrayList(MOV_ITEMS, movieItems);
    }

    private void reloadInstance(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            m_lastPage = savedInstanceState.getBoolean(LAST_PAGE);
            m_currentPage = savedInstanceState.getInt(CURR_PAGE);
            m_previousTotal = savedInstanceState.getInt(PREV_TOT);
            m_previous_sort_mode = savedInstanceState.getString(PREV_SORT);
            ArrayList<MovieItem> movieItems = savedInstanceState.getParcelableArrayList(MOV_ITEMS);

            if ( movieItems != null ) {
                // if update movies has been called before, not update in postupdate
                if ( m_isLoading ){ m_isLoading = false; }
                m_movieAdapter.clear();
                m_movieAdapter.addAll(movieItems);
                m_gridView.setSelection(savedInstanceState.getInt(CURR_POS));
                Log.d(LOG_TAG, "Restored items: " + String.valueOf(movieItems.size()));
                Log.d(LOG_TAG, "Restored lastpage: " + String.valueOf(m_currentPage));
                Log.d(LOG_TAG, "Restored position: " + savedInstanceState.getInt(CURR_POS));
            }
        }else {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String sort_mode = prefs.getString(getString(R.string.pref_header_sort_key),
                    getString(R.string.pref_header_sort_key_default));
            String fav_key = getString(R.string.pref_header_sort_key_favorite);
            if( sort_mode.equals(fav_key) ){ updateMovies(m_currentPage); }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_movies, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                m_previous_sort_mode = prefs.getString(getString(R.string.pref_header_sort_key),
                        getString(R.string.pref_header_sort_key_default));
                Intent settings = new Intent(getActivity(), SettingsActivity.class);
                startActivity(settings);
                return true;
            case R.id.action_refresh:
                resetPage();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void resetPage() {
        if (!m_isLoading) {
            m_movieAdapter.clear();
            getActivity().setTitle(getString(R.string.app_name));
            if( isTwoPanel(getActivity()) ) {
                android.support.v4.app.FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.container, new Fragment());
                ft.commit();
            }
            m_currentPage = 1;
            m_lastPage = false;
            m_previousTotal = 0;
            updateMovies(m_currentPage);
        }
    }

    private void updateMovies(int current_page) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sort_mode = prefs.getString(getString(R.string.pref_header_sort_key),
                getString(R.string.pref_header_sort_key_default));
        String fav_key = getString(R.string.pref_header_sort_key_favorite);

        if( sort_mode.equals(fav_key) ){
            m_movieAdapter.clear();
            m_movieAdapter.notifyDataSetChanged();

            //look only for favorites
            //get the list
            String[] favList = Favorites.getFavoriteList(getActivity());
            Log.d(LOG_TAG, "Fav List = " + Arrays.toString(favList));

            //start the calls
            MoviesService client = MoviesServiceGenerator.createService(MoviesService.class, getContext());
            for( String movie: favList) {
                int movie_id = Integer.parseInt(movie);

                Call<MovieItem> call = client.summary(movie_id, null);
                call.enqueue(new Callback<MovieItem>() {
                    @Override
                    public void onResponse(Response<MovieItem> response) {
                        Log.d(LOG_TAG, "Status Code = " + response.code());
                        if (response.isSuccess()) {
                            // request successful (status code 200, 201)
                            MovieItem movie_sum = response.body();
                            Log.d(LOG_TAG, "Get MovieItem found= " + movie_sum.id);

                            m_movieAdapter.add(movie_sum);
                            m_movieAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Log.d(LOG_TAG, "Get MovieItem Failed: " + t.toString());
                    }
                });
            } // enf for fav list
        }else {
            //download from internet the movie list
            FetchMoviesTask moviesTask = new FetchMoviesTask(getContext(), this);

            String current_page_str = Integer.toString(current_page);
            m_isLoading = true;
            moviesTask.execute(sort_mode, current_page_str);
            m_currentPage++;
            if (m_currentPage + 1 > MAX_PAGE) {
                m_lastPage = true;
            }else{ m_lastPage = false; }
        }
    }

    public void postupdateMovies(ArrayList<MovieItem> result) {
        if (m_isLoading && result != null) {
            m_movieAdapter.addAll(result);
            m_movieAdapter.notifyDataSetChanged();
            Log.d(LOG_TAG, "Update grid: " + result.size());
        }else{ Log.d(LOG_TAG, "No update grid"); }
        m_isLoading = false;
    }

} // end of MoviesGridFragment class
