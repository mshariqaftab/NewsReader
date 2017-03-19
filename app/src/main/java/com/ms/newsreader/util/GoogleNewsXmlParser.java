package com.ms.newsreader.util;

/**
 * Created by Mohd. Shariq on 18/10/16.
 */


import android.util.Xml;

import com.ms.newsreader.adapter.GoogleFeed;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class parses XML feeds from new.google.com.
 * Given an InputStream representation of a feed, it returns a List of entries,
 * where each list element represents a single entry (post) in the XML feed.
 */
public class GoogleNewsXmlParser {
    private static final String ns = null;

    public List<GoogleFeed> parse(InputStream in) throws XmlPullParserException, IOException, ParseException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readEntry(parser);
        } finally {
            in.close();
        }
    }

    // Parses the contents of an entry. If it encounters a category, title, publish date, link tag and description hands them
    // off to their respective &quot;read&quot; methods for processing. Otherwise, skips the tag.
    private List<GoogleFeed> readEntry(XmlPullParser parser) throws XmlPullParserException, IOException, ParseException {
        List<GoogleFeed> items = new ArrayList<>();
        parser.require(XmlPullParser.START_TAG, ns, "rss");
        String title = null;
        String category = null;
        String publishDate = null;
        String link = null;
        String description = null;
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("category")) {
                category = readNewsCategory(parser);
            } else if (name.equals("title")) {
                title = readTitle(parser);
            } else if (name.equals("pubDate")) {
                publishDate = readPublishDate(parser);
            } else if (name.equals("link")) {
                link = readLink(parser);
            } else if (name.equals("description")) {
                description = readDescription(parser);
            }

            if (category != null && title != null && publishDate != null && link != null && description != null) {
                GoogleFeed googleFeed = new GoogleFeed();
                googleFeed.setNewsCategory(category);
                googleFeed.setNewsTitle(title);
                googleFeed.setPublishDate(publishDate);
                googleFeed.setLink(link);
                googleFeed.setDescription(description);
                items.add(googleFeed);
                category = null;
                title = null;
                publishDate = null;
                link = null;
                description = null;
            }
        }
        return items;
    }

    // Processes category tags in the feed.
    private String readNewsCategory(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "category");
        String newsCategory = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "category");
        return newsCategory;
    }

    // Processes title tags in the feed.
    private String readTitle(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "title");
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "title");
        return title;
    }

    // Processes description tags in the feed.
    private String readDescription(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "description");
        String description = readText(parser);
        Pattern p = Pattern.compile("src=[\\\"']([^\\\"^']*)");
        Matcher m = p.matcher(description);
        String srcTag = "";
        while (m.find()) {
            String src = m.group();
            int startIndex = src.indexOf("src=") + 5;
            srcTag = src.substring(startIndex, src.length());
        }
        parser.require(XmlPullParser.END_TAG, ns, "description");
        return "http:" + srcTag;
    }

    // Processes link tags in the feed.
    private String readLink(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "link");
        String link = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "link");
        return link;
    }

    // Processes PubDate tags in the feed.
    private String readPublishDate(XmlPullParser parser) throws IOException, XmlPullParserException, ParseException {
        parser.require(XmlPullParser.START_TAG, ns, "pubDate");
        String pubDate = readText(parser);
        SimpleDateFormat sdf = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
        SimpleDateFormat format = new SimpleDateFormat("E,dd MMM yyyy");
        Date date = sdf.parse(pubDate);
        pubDate = format.format(date);
        parser.require(XmlPullParser.END_TAG, ns, "pubDate");

        return pubDate;
    }

    // For the tags title, category and description, extracts their text values.
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    // NOT IN USE FOR NOW
    // Skips tags the parser isn't interested in. Uses depth to handle nested tags. i.e.,
    // if the next tag after a START_TAG isn't a matching END_TAG, it keeps going until it
    // finds the matching END_TAG (as indicated by the value of "depth" being 0).
    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}
