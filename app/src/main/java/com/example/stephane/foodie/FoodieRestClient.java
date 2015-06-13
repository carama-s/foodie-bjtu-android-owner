package com.example.stephane.foodie;

import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;

/**
 * Created by Stéphane on 01/05/2015.
 */

public class FoodieRestClient {

    private static final String BASE_URL = "http://foodie.dennajort.fr/api/";
    private static final String BASE_URL_OAUTH = "http://foodie.dennajort.fr/oauth/";

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void addHeader(String value) {
        if (value != "") {
            client.addHeader("Authorization", "Bearer " + value);
        }
    }

    public static RequestHandle get(String url, RequestParams params, String token, AsyncHttpResponseHandler responseHandler) {
        addHeader(token);
        return client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static RequestHandle post(String url, RequestParams params, String token, AsyncHttpResponseHandler responseHandler) {
        addHeader(token);
        return client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    public static RequestHandle put(String url, RequestParams params, String token, AsyncHttpResponseHandler responseHandler) {
        addHeader(token);
        return client.put(getAbsoluteUrl(url), params, responseHandler);
    }

    public static RequestHandle delete(String url, String token, AsyncHttpResponseHandler responseHandler) {
        addHeader(token);
        return client.delete(getAbsoluteUrl(url), responseHandler);
    }

    public static RequestHandle postOauth(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        return client.post(getAbsoluteUrlOauth(url), params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }

    private static String getAbsoluteUrlOauth(String relativeUrl) {
        return BASE_URL_OAUTH + relativeUrl;
    }
}