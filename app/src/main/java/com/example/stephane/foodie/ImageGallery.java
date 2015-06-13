package com.example.stephane.foodie;

import android.graphics.drawable.Drawable;
import android.net.Uri;

/**
 * Created by Stéphane on 07/05/2015.
 */
public class ImageGallery {

    private Uri     mUri = null;
    private String  mUrl = null;

    ImageGallery(Uri uri) {
        mUri = uri;
    }

    ImageGallery(String url) {
        mUrl = url;
    }

    public Uri getUri() {
        return mUri;
    }

    public String getUrl() {
        return mUrl;
    }
}
