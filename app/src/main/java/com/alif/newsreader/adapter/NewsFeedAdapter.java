package com.alif.newsreader.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alif.newsreader.R;
import com.alif.newsreader.activities.NewsDetailsActivity;

import java.util.List;


public class NewsFeedAdapter extends RecyclerView.Adapter<NewsFeedAdapter.ViewHolder> {

    private static final String DEBUG_TAG = NewsFeedAdapter.class.getSimpleName();
    private List<GoogleFeed> newsFeedList;
    private final Context mContext;

    public void add(GoogleFeed feedObj, int position) {
        position = position == -1 ? getItemCount() : position;
        newsFeedList.add(position, feedObj);
        notifyItemInserted(position);
    }

    public void remove(int position) {
        if (position < getItemCount()) {
            newsFeedList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public NewsFeedAdapter(Context mContext, List<GoogleFeed> newsFeedList) {
        this.mContext = mContext;
        this.newsFeedList = newsFeedList;
    }

    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_feed_list_row, parent, false);

        return new ViewHolder(view, new ViewHolder.NewsFeedViewHolderClicks() {
            @Override
            public void onFeedRowClick(View caller, int position) {
                Intent intent = new Intent(mContext, NewsDetailsActivity.class);
                intent.putExtra("URL", newsFeedList.get(position - 1).getLink());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final GoogleFeed googleFeed = newsFeedList.get(position);
        holder.title.setText(googleFeed.getNewsTitle());
    }

    @Override
    public int getItemCount() {
        Log.d(DEBUG_TAG, "" + newsFeedList.size());
        return newsFeedList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView title;
        public NewsFeedViewHolderClicks mListener;

        public ViewHolder(View view, NewsFeedViewHolderClicks mListener) {
            super(view);
            title = (TextView) view.findViewById(R.id.newsTitle);
            this.mListener = mListener;
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            Log.d(DEBUG_TAG, "" + position);
            mListener.onFeedRowClick(v, position);
        }


        public static interface NewsFeedViewHolderClicks {
            public void onFeedRowClick(View caller, int position);
        }
    }
}
