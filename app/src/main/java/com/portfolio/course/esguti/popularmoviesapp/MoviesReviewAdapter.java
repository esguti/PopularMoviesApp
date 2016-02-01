package com.portfolio.course.esguti.popularmoviesapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.portfolio.course.esguti.popularmoviesapp.movie.Reviews.Review;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by esguti on 29.01.16.
 */
public class MoviesReviewAdapter extends RecyclerView.Adapter<MoviesReviewAdapter.ViewHolder> {
    private final String LOG_TAG = MoviesReviewAdapter.class.getSimpleName();

    private List<Review> m_reviews;
    private LayoutInflater m_inflater;

    MoviesReviewAdapter(Context context) {
        m_inflater = LayoutInflater.from(context);
        m_reviews = new ArrayList<>();
    }

    public List<Review> getReviewList() {
        return m_reviews;
    }

    public void setReviewList(List<Review> reviewList) {
        m_reviews.clear();
        m_reviews.addAll(reviewList);
        this.notifyItemRangeInserted(0, m_reviews.size() - 1);
        //notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return (m_reviews == null) ? 0 : m_reviews.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView m_author;
        private final TextView m_content;

        public ViewHolder(View view) {
            super(view);
            m_author = (TextView) view.findViewById(R.id.movie_detail_review_author);
            m_content = (TextView) view.findViewById(R.id.movie_detail_review_content);
        }
    }

    @Override
    public MoviesReviewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = m_inflater.inflate(R.layout.movie_review, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.m_author.setText(m_reviews.get(position).author);
        holder.m_content.setText(m_reviews.get(position).content);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
