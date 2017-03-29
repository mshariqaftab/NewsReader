package com.ms.newsreader.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ms.newsreader.R;
import com.ms.newsreader.activities.NewsDetailsActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

public class NewsFeedAdapter extends RecyclerView.Adapter<NewsFeedAdapter.ViewHolder> {
    private List<GoogleFeed> newsFeedList;
    private final Context mContext;

    public void add(GoogleFeed googleFeed, int position) {
        position = position == -1 ? getItemCount() : position;
        newsFeedList.add(position, googleFeed);
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

    public ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_feed_list_row, parent, false);

        return new ViewHolder(view, new ViewHolder.NewsFeedViewHolderClicks() {
            @Override
            public void onFeedRowClick(View caller, int position) {
                Intent intent = new Intent(mContext, NewsDetailsActivity.class);
                intent.putExtra("BASE_URL", newsFeedList.get(position).getLink());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final GoogleFeed googleFeed = newsFeedList.get(position);
        holder.title.setText(googleFeed.getNewsTitle());
        holder.pubDate.setText(googleFeed.getPublishDate());
        Picasso.with(mContext).load(googleFeed.getDescription()).placeholder(R.mipmap.ic_launcher).error(R.mipmap.ic_launcher).into(holder.newsImage);
    }

    @Override
    public int getItemCount() {
        return newsFeedList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView title, pubDate;
        public final ImageView newsImage;
        public NewsFeedViewHolderClicks mListener;

        public ViewHolder(View view, NewsFeedViewHolderClicks mListener) {
            super(view);
            title = (TextView) view.findViewById(R.id.news_title);
            newsImage = (ImageView) view.findViewById(R.id.news_image);
            pubDate = (TextView) view.findViewById(R.id.pub_date);
            this.mListener = mListener;
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            mListener.onFeedRowClick(v, position);
        }

        public static interface NewsFeedViewHolderClicks {
            public void onFeedRowClick(View caller, int position);
        }
    }
}
