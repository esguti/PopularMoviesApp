package com.portfolio.course.esguti.popularmoviesapp;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.HashSet;

/**
 * Created by esguti on 08.12.15.
 */
public class MoviesItemAdapter extends ArrayAdapter<MovieItem> {
    private final String LOG_TAG = MoviesItemAdapter.class.getSimpleName();

    private HashSet<Integer> m_id_set;

    public MoviesItemAdapter(Activity context) {
        super(context, 0);
        m_id_set = new HashSet<>();
    }

    /**
     * Provides a view for an AdapterView (ListView, GridView, etc.)
     *
     * @param position    The AdapterView position that is requesting a view
     * @param convertView The recycled view to populate.
     *                    (search online for "android view recycling" to learn more)
     * @param parent      The parent ViewGroup that is used for inflation.
     * @return The View for the position in the AdapterView.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Adapters recycle views to AdapterViews.
        // If this is a new View object we're getting, then inflate the layout.
        // If not, this view already has the layout inflated from a previous call to getView,
        // and we modify the View widgets as usual.
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.movie_item, parent, false);
        }

        // Gets the MovieITem object from the ArrayAdapter at the appropriate position
        MovieItem movieItem = getItem(position);

        ImageView iconView = (ImageView) convertView.findViewById(R.id.movie_item_image);

        // Use Picasso to manage the images;
        Picasso.with(getContext()).load(movieItem.getPosterPath(getContext())).into(iconView);

        TextView movieNameView = (TextView) convertView.findViewById(R.id.movie_item_text);
        movieNameView.setText(movieItem.getTitle());

        return convertView;
    }
}
