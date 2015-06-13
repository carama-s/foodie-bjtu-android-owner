package com.example.stephane.foodie;

import android.os.Parcel;
import android.os.Parcelable;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by Stéphane on 06/05/2015.
 */

public class Offer implements Parcelable {

    private String  mId;
    private String  mDesignation;
    private String  mDescription;
    private String  mExpirationDate;

    Offer(Parcel in) {
        mId = in.readString();
        mDesignation = in.readString();
        mDescription = in.readString();
        mExpirationDate = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mId);
        parcel.writeString(mDesignation);
        parcel.writeString(mDescription);
        parcel.writeString(mExpirationDate);
    }

    public Offer(JSONObject object) {
        try {
            mId = object.getString("id");
            mDesignation = object.getString("name");
            mDescription = object.getString("description");
            mExpirationDate = object.getString("expiration_date");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Offer> fromJson(JSONArray jsonObjects) {
        ArrayList<Offer> offers = new ArrayList<Offer>();
        for (int i = 0; i < jsonObjects.length(); i++) {
            try {
                offers.add(new Offer(jsonObjects.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return offers;
    }

    public String getId() {
        return this.mId;
    }

    public String getDesignation() {
        return this.mDesignation;
    }

    public String getDescription() {
        return this.mDescription;
    }

    public String getExpirationDate() {

        Date newDate = null;
        try {
            newDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(mExpirationDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (newDate == null) { return null; }

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        String date = format.format(newDate);

        return date;
    }
}