package com.alif.newsreader.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alif.newsreader.R;

import java.util.List;


public class NewsFeedAdapter extends RecyclerView.Adapter<NewsFeedAdapter.FeedSectionHolder> {

    private List<GoogleFeed> newsFeedList;

    public NewsFeedAdapter(List<GoogleFeed> newsFeedList) {
        this.newsFeedList = newsFeedList;
    }


    @Override
    public FeedSectionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_feed_list_row, parent, false);
        return new FeedSectionHolder(v);
    }

    @Override
    public void onBindViewHolder(FeedSectionHolder holder, int position) {
        GoogleFeed googleFeed = newsFeedList.get(position);
       // holder.category.setText(googleFeed.getNewsCategory());
        holder.title.setText(googleFeed.getNewsTitle());
        //holder.link.setText(googleFeed.getLink());
    }

    @Override
    public int getItemCount() {
        return newsFeedList.size();
    }

    public class FeedSectionHolder extends RecyclerView.ViewHolder {

        final TextView category, title, link;

        public FeedSectionHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            category = (TextView) itemView.findViewById(R.id.title);
            link = (TextView) itemView.findViewById(R.id.title);
             //description = (TextView) itemView.findViewById(R.id.title);
        }
    }
}
