package com.ms.newsreader.util;

import android.net.Uri;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Mohd. Shariq on 13/10/16.
 */

public class Constant {
    public static final String BASE_URL = "https://news.google.com/news";  // Google feed BASE_URL
    public static final String ADS_ID = "118F106CA47E3799F5997AFCAFADEFCF"; // valid only for debugging, Remove it while releasing the app

    // Type of news feeds
    private static final String output_type = "rss";

    /* Number of google news to be displayed by default */
    // TODO: Need to make it dynamic
    private static final int numberOfNews = 20;

    /* The country parameter allows us to get country news */
    private static final String COUNTRY_PARAM = "ned";

    /* The language parameter allows us to get news in different language */
    private static final String LANGUAGE_PARAM = "hl";

    /* The topic parameter allows us to get news of different topics */
    private static final String TOPIC_PARAM = "topic";

    /* The output parameter allows us to get news RSS feeds */
    private static final String OUTPUT_PARAM = "output";

    /* The num parameter allows us to get fix number of news */
    private static final String NUMBER_PARAM = "num";



    /* Types of news we want our API to return */
    public static final String NEWS_FEED_TECHNOLOGY = "tc";
    public static final String NEWS_FEED_BUSINESS = "b";
    public static final String NEWS_FEED_ENTERTAINMENT = "e";
    public static final String NEWS_FEED_SPORTS = "s";
    public static final String NEWS_FEED_HEALTH = "m";
    public static final String NEWS_FEED_WORLD = "w";
    public static final String NEWS_FEED_SCIENCE = "snc";
    public static final String NEWS_FEED_TOP_STORIES = "";

    /* Name of News tab keys */
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



    public static URL buildUrlWithTopic(String newsTopic) {
        Uri newsQueryUri = Uri.parse(BASE_URL).buildUpon()
                .appendQueryParameter(OUTPUT_PARAM, output_type)
                .appendQueryParameter(NUMBER_PARAM, Integer.toString(numberOfNews))
                .appendQueryParameter(COUNTRY_PARAM, "in")
                .appendQueryParameter(LANGUAGE_PARAM, "en")
                .appendQueryParameter(TOPIC_PARAM, newsTopic)
                .build();
        try {
            URL weatherQueryUrl = new URL(newsQueryUri.toString());
            Log.v("URL ", "URL: " + weatherQueryUrl);
            return weatherQueryUrl;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

}
