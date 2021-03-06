package com.portfolio.course.esguti.popularmoviesapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.portfolio.course.esguti.popularmoviesapp.movie.Trailers.Trailer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by esguti on 29.01.16.
 */
public class MoviesTrailerAdapter extends RecyclerView.Adapter<MoviesTrailerAdapter.ViewHolder> {
    private final String LOG_TAG = MoviesTrailerAdapter.class.getSimpleName();

    private List<Trailer> m_trailers;
    private LayoutInflater m_inflater;
    private Context m_context;

    MoviesTrailerAdapter(Context context) {
        m_inflater = LayoutInflater.from(context);
        m_trailers = new ArrayList<>();
        m_context = context;
    }

    public List<Trailer> getTrailerList() {
        return m_trailers;
    }

    public void setTrailerList(List<Trailer> trailerList) {
        m_trailers.clear();
        m_trailers.addAll(trailerList);
        this.notifyItemRangeInserted(0, m_trailers.size() - 1);
    }

    @Override
    public int getItemCount() {
        return (m_trailers == null) ? 0 : m_trailers.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView m_text;
        private final ImageView m_icon;

        public ViewHolder(View view) {
            super(view);
            m_text = (TextView) view.findViewById(R.id.movie_detail_trailer_text);
            m_icon = (ImageView) view.findViewById(R.id.movie_detail_trailer_icon);
        }
    }

    @Override
    public MoviesTrailerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = m_inflater.inflate(R.layout.movie_trailer, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.m_text.setText(m_trailers.get(position).name);
        holder.m_text.setTag(m_trailers.get(position).key);
        holder.m_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openVideo(v);
            }
        });
        holder.m_icon.setTag(m_trailers.get(position).key);
        holder.m_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { openVideo(v); }
        });
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    private void openVideo(View view) {
        //Get url from tag
        String id = (String) view.getTag();
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse( m_context.getString(R.string.trailer_video_root) + id));
        m_context.startActivity(intent);
    }

}
