package com.alif.newsreader.Interface;

import android.view.View;

/**
 * Created by Mohd. Shariq on 08/10/16.
 */

public interface ClickListener {
    void onClick(View view, int position);

    void onLongClick(View view, int position);
}
