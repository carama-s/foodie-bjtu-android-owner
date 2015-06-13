package com.example.stephane.foodie;

import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;

/**
 * Created by Stéphane on 06/05/2015.
 */
public class Restaurant implements Parcelable {

    private String                              mId;
    private String                              mName;
    private RestaurantImage                     mImage;
    private ArrayList<RestaurantImage>          mGallery;
    private RequestHandle                       mainPictureHandler = null;
    private RequestHandle                       galleryHandler = null;
    private ArrayList<onMainPictureCallback>    onMainPictureCallbacks = null;
    private ArrayList<onGalleryCallback>        onGalleryCallbacks = null;

    Restaurant(Parcel in) {
        mId = in.readString();
        mName = in.readString();
        if (in.readByte() == 1) {
            mImage = in.readParcelable(RestaurantImage.class.getClassLoader());
        }
        if (in.readByte() == 1) {
            RestaurantImage[] tmpGallery = (RestaurantImage []) in.readParcelableArray(RestaurantImage.class.getClassLoader());
            mGallery = new ArrayList<RestaurantImage>(Arrays.asList(tmpGallery));
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mId);
        parcel.writeString(mName);
        if (mImage == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeParcelable(mImage, i);
        }
        if (mGallery == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeParcelableArray(mGallery.toArray(new RestaurantImage[mGallery.size()]), i);
        }
    }

    public interface onMainPictureCallback {
        void onInfo(RestaurantImage image);
    }

    public interface onGalleryCallback {
        void onInfo(ArrayList<RestaurantImage> images);
    }

    public Restaurant(JSONObject object) {
        try {
            mId = object.getString("id");
            mName = object.getString("name");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void onGalleryInfo(onGalleryCallback cb) {
        onGalleryInfo(cb, false);
    }

    public void onGalleryInfo(onGalleryCallback cb, boolean forceRefresh) {
        if (forceRefresh) {
            onGalleryCallbacks = null;
            mGallery = null;
            if (galleryHandler != null && !galleryHandler.isFinished()) {
                galleryHandler.cancel(true);
                galleryHandler = null;
            }
        }

        if (onGalleryCallbacks == null) {
            onGalleryCallbacks = new ArrayList<onGalleryCallback>();
            galleryHandler = FoodieRestClient.get("restaurants/" + mId + "/gallery", null, "", new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                    mGallery = new ArrayList<RestaurantImage>();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            mGallery.add(new RestaurantImage(response.getJSONObject(i)));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    for (int i = 0; i < onGalleryCallbacks.size(); i++) {
                        onGalleryCallbacks.get(i).onInfo(mGallery);
                    }
                    onGalleryCallbacks.clear();
                }

                @Override
                public void onFinish() {
                    galleryHandler = null;
                }
            });
        }
        if (mGallery != null) {
            cb.onInfo(mGallery);
            return;
        }
        onGalleryCallbacks.add(cb);
    }

    public void onMainPictureInfo(onMainPictureCallback cb) {
        onMainPictureInfo(cb, false);
    }

    public void onMainPictureInfo(onMainPictureCallback cb, boolean forceRefresh) {
        if (forceRefresh) {
            onMainPictureCallbacks = null;
            mImage = null;
            if (mainPictureHandler != null && !mainPictureHandler.isFinished()) {
                mainPictureHandler.cancel(true);
                mainPictureHandler = null;
            }
        }
        if (onMainPictureCallbacks == null) {
            onMainPictureCallbacks = new ArrayList<onMainPictureCallback>();
            mainPictureHandler = FoodieRestClient.get("restaurants/" + mId + "/main_picture", null, "", new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    mImage = new RestaurantImage(response);
                    for (int i = 0; i < onMainPictureCallbacks.size(); i++) {
                        onMainPictureCallbacks.get(i).onInfo(mImage);
                    }
                    onMainPictureCallbacks.clear();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    if (statusCode == 404) {
                        mImage = null;
                    }
                }

                @Override
                public void onFinish() {
                    mainPictureHandler = null;
                }
            });
        }
        if (mImage != null) {
            cb.onInfo(mImage);
            return;
        }
        onMainPictureCallbacks.add(cb);
    }

    public static ArrayList<Restaurant> fromJson(JSONArray jsonObjects) {
        ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
        for (int i = 0; i < jsonObjects.length(); i++) {
            try {
                restaurants.add(new Restaurant(jsonObjects.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return restaurants;
    }

    public String getId() {
        return this.mId;
    }

    public String getName() {
        return this.mName;
    }

    public RestaurantImage getRestaurantImage() { return mImage; }

    public ArrayList<RestaurantImage> getGallery() { return mGallery; }

}