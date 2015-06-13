package com.example.stephane.foodie;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

/**
 * Created by Stéphane on 08/05/2015.
 */
public class RestaurantImage implements Parcelable {

    private String  mId;
    private String  mUrl;
    private boolean mMain;

    RestaurantImage(JSONObject object) {
        try {
            mId = object.getString("id");
            mUrl = object.getString("url");
            mMain = object.getBoolean("main");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    RestaurantImage(Parcel in) {
        if (in.readByte() == 1) {
            mId = in.readString();
            mUrl = in.readString();
            mMain = in.readByte() != 0;
        }
    }

    public String getId() {
        return mId;
    }

    public String getFullUrl() {
        if (mUrl == null) return null;
        return "http://foodie.dennajort.fr" + mUrl;
    }

    public boolean isMain() {
        return mMain;
    }

    public String getUrl() {
        return mUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        if (mId == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeString(mId);
            parcel.writeString(mUrl);
            parcel.writeByte((byte) (mMain ? 1 : 0));
        }
    }
}
