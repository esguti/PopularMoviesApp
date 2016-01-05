package com.portfolio.course.esguti.popularmoviesapp;

import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.GridView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;


/**
 * A fragment containing the grid view of Android versions.
 */
public class MoviesGridFragment extends Fragment {

    private final String LOG_TAG = MoviesGridFragment.class.getSimpleName();

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_movies_grid, container, false);

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
                Intent movieIntent = new Intent(getActivity(), MovieDetailActivity.class);
                movieIntent.putExtra(MovieItem.class.getName(), movie);

                // Verify that the intent will resolve to an activity
                if (movieIntent.resolveActivity(getContext().getPackageManager()) != null) {
                    startActivity(movieIntent);
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
                if (!m_lastPage && !m_isLoading
                        && (totalItemCount - VISIBLE_THRESHOLD) <= (firstVisibleItem + visibleItemCount)) {
                    updateMovies(m_currentPage);
                }
            }
        });

        String clean = "null";
        if( savedInstanceState != null ) clean = savedInstanceState.toString();
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
    public void onResume(){
        super.onResume();

        //reset in case of sort mode change
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String currrent_sort_mode = prefs.getString(getString(R.string.pref_header_sort_key),
                getString(R.string.pref_header_sort_key_default));
        if( m_previous_sort_mode != null && !m_previous_sort_mode.equals(currrent_sort_mode) ){
            resetPage();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    private void saveInstance(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        savedInstanceState.putInt("currentPos",m_gridView.getFirstVisiblePosition());
        savedInstanceState.putBoolean("m_lastPage", m_lastPage);
        savedInstanceState.putInt("m_currentPage", m_currentPage);
        savedInstanceState.putInt("m_previousTotal", m_previousTotal);
        savedInstanceState.putString("m_previous_sort_mode", m_previous_sort_mode);
        ArrayList<MovieItem> movieItems = new ArrayList<>();
        for (int i = 0; i < m_movieAdapter.getCount(); i++) {
            movieItems.add(m_movieAdapter.getItem(i));
        }
        savedInstanceState.putParcelableArrayList("m_movieItems", movieItems);
    }

    private void reloadInstance(Bundle savedInstanceState){
        if( savedInstanceState != null) {
            m_lastPage = savedInstanceState.getBoolean("m_lastPage");
            m_currentPage = savedInstanceState.getInt("m_currentPage");
            m_previousTotal = savedInstanceState.getInt("m_previousTotal");
            m_previous_sort_mode = savedInstanceState.getString("m_previous_sort_mode");
            ArrayList<MovieItem> movieItems = savedInstanceState.getParcelableArrayList("m_movieItems");
            if( movieItems != null ) {
                // if update movies has been called before, not update in postupdate
                if ( m_isLoading ){ m_isLoading = false; }
                m_movieAdapter.clear();
                m_movieAdapter.addAll(movieItems);
                m_gridView.setSelection(savedInstanceState.getInt("currentPos"));
                Log.d(LOG_TAG, "Restored items: " + String.valueOf(movieItems.size()));
                Log.d(LOG_TAG, "Restored lastpage: " + String.valueOf(m_currentPage));
                Log.d(LOG_TAG, "Restored position: " + savedInstanceState.getInt("currentPos"));
            }
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

        switch (id){
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


    private void resetPage(){
        if( !m_isLoading ) {
            m_movieAdapter.clear();
            m_currentPage = 1;
            m_lastPage = false;
            m_previousTotal = 0;
            updateMovies(m_currentPage);
        }
    }

    private void updateMovies(int current_page){
        FetchMoviesTask moviesTask = new FetchMoviesTask(getContext(), this);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sort_mode = prefs.getString(getString(R.string.pref_header_sort_key),
                getString(R.string.pref_header_sort_key_default));
        String current_page_str = Integer.toString(current_page);
        m_isLoading = true;
        moviesTask.execute(sort_mode, current_page_str);
        m_currentPage++;
        if (m_currentPage + 1 > MAX_PAGE) { m_lastPage = true; }
    }

    public void postupdateMovies(ArrayList<MovieItem> result) {
        if( m_isLoading && result != null) {
            m_movieAdapter.addAll(result);
            Log.d(LOG_TAG, "Update grid: " + result.size());
        }else{ Log.d(LOG_TAG, "No update grid"); }
        m_isLoading = false;
    }

} // end of MoviesGridFragment class
