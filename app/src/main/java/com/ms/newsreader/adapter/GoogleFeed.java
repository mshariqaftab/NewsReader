package com.ms.newsreader.adapter;

import java.io.Serializable;

public class GoogleFeed implements Serializable {
    private String newsCategory;
    private String newsTitle;
    private String link;
    private String newsImage;
    private String publishDate;

    public String getNewsCategory() {
        return newsCategory;
    }

    public void setNewsCategory(String newsCategory) {
        this.newsCategory = newsCategory;
    }

    public String getNewsTitle() {
        return newsTitle;
    }

    public void setNewsTitle(String newsTitle) {
        this.newsTitle = newsTitle;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getNewsImage() {
        return newsImage;
    }

    public void setNewsImage(String newsImage) {
        this.newsImage = newsImage;
    }

    public String getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(String publishDate) {
        this.publishDate = publishDate;
    }
}
