package com.ms.newsreader.util;

/**
 * Created by Mohd. Shariq on 13/10/16.
 */

public class Constant {
    public static final String URL = "https://news.google.com/news?output=rss&num=20&ned=in&hl=en&topic=";  // Google feed URL
    public static final String ADS_ID = "118F106CA47E3799F5997AFCAFADEFCF"; // valid only for debugging, Remove it while releasing the app
    public static final String NEWS_FEED_TECHNOLOGY = "tc";
    public static final String NEWS_FEED_BUSINESS = "b";
    public static final String NEWS_FEED_ENTERTAINMENT = "e";
    public static final String NEWS_FEED_SPORTS = "s";
    public static final String NEWS_FEED_HEALTH = "m";
    public static final String NEWS_FEED_WORLD = "w";
    public static final String NEWS_FEED_SCIENCE = "snc";
    public static final String NEWS_FEED_TOP_STORIES = "";

    // News tab keys
    public static final String TAB_TOP_STORIES_KEY = "Top Stories";
    public static final String TAB_BUSINESS_KEY = "Business";
    public static final String TAB_HEALTH_KEY = "Health";
    public static final String TAB_SPORT_KEY = "Sports";
    public static final String TAB_WORLD_KEY = "World";
    public static final String TAB_SCIENCE_KEY = "science";
    public static final String TAB_ENTERTAINMENT_KEY = "entertainment";
    public static final String TAB_TECHNOLOGY_KEY = "technology";

    public static final String WIFI = "Wi-Fi";
    public static final String ANY = "Any";
    public static final String LIST_PREF = "listPref";
    public static final String FEED_TYPE_KEY = "type";

    // Whether there is a Wi-Fi connection.
    public static boolean WIFI_CONNECTED = false;

    // Whether there is a mobile connection.
    public static boolean MOBILE_CONNECTED = false;

    // Whether the display should be refreshed.
    public static boolean REFRESH_DISPLAY = true;

    // The user's current network preference setting.
    public static String NETWORK_PREFERENCE = null;

}
