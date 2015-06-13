package com.example.stephane.foodie;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.inputmethod.InputMethodManager;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by Stéphane on 26/04/2015.
 */

public class MyUtils {

    private static Activity         mActivity = null;
    private static DisplayMetrics   mDm = null;
    private static Picasso          mPicasso = null;

    public static void setActivity(Activity activity) {
        mActivity = activity;
        Display d = activity.getWindowManager().getDefaultDisplay();
        mDm = new DisplayMetrics();
        d.getMetrics(mDm);
    }

    public static Picasso getPicasso() {
        if (mPicasso == null) {
            mPicasso = new Picasso.Builder(mActivity).listener(new Picasso.Listener() {
                @Override
                public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                }
            }).build();
        }
        return mPicasso;
    }


    public static DisplayMetrics getDisplayMetrics() {
        return mDm;
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    public static int getHeightRatio_16_9() {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, Math.round(MyUtils.pxToDp(mDm.widthPixels) / (16f / 9f)), mActivity.getResources().getDisplayMetrics());
    }

    public static int getHeightRatio_16_9(int width) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, Math.round(MyUtils.pxToDp(width) / (16f / 9f)), mActivity.getResources().getDisplayMetrics());
    }

    public static int getHeightRatio_4_3() {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, Math.round(MyUtils.pxToDp(mDm.widthPixels) / (4f / 3f)), mActivity.getResources().getDisplayMetrics());
    }

    public static int getHeightRatio_4_3(int width) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, Math.round(MyUtils.pxToDp(width) / (4f / 3f)), mActivity.getResources().getDisplayMetrics());
    }

    public static int getSpanImageGallery() {
        return 3;
    }

    public static int getWidthImageGallery(int span) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, Math.round((MyUtils.pxToDp(mDm.widthPixels) / span)), mActivity.getResources().getDisplayMetrics());
    }

    public static int getHeightImageGallery(int span) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, Math.round((MyUtils.pxToDp(mDm.widthPixels) / span) * 0.75), mActivity.getResources().getDisplayMetrics());
    }

    public static void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) mActivity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(mActivity.getWindow().getDecorView().getRootView().getWindowToken(), 0);
    }

    public static String getAccessTokenFromPreferences() {
        return mActivity.getSharedPreferences(AuthentificationActivity.PREFS_NAME, 0).getString("access_token", "");
    }

    // google map functions

    public static List<Address> findLocationByAddress(Activity activity, String address) {

        Geocoder geoCoder = new Geocoder(activity, Locale.getDefault());

        try {
            List<Address> addresses = geoCoder.getFromLocationName(address, 5);
            if (addresses != null && addresses.size() > 0) {
                return addresses;
            }
        } catch (IOException e) {
            Log.e("findLocationByAddress", "Unable to connect to Geocoder", e);
        }
        return null;
    }

    public static Address findLocationByCoordinates(Activity activity, double latitude, double longitude ) {
        Geocoder geoCoder = new Geocoder(activity.getApplicationContext(), Locale.getDefault());

        try {
            List<Address> addresses = geoCoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                return addresses.get(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String getAddressLine(Address address) {
        String addressLine = "";
        for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
            addressLine += address.getAddressLine(i);
            if (i < address.getMaxAddressLineIndex()) {
                addressLine += "\n";
            }
        }
        return addressLine;
    }

    public static String getPathFromMediaUri(Context context, Uri uri) {
        String result = null;

        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        int col = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
        if (col >= 0 && cursor.moveToFirst())
            result = cursor.getString(col);
        cursor.close();

        return result;
    }
}
