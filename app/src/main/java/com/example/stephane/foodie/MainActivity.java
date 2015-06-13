package com.example.stephane.foodie;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;
import it.neokree.materialnavigationdrawer.elements.MaterialAccount;
import it.neokree.materialnavigationdrawer.elements.MaterialSection;
import it.neokree.materialnavigationdrawer.elements.listeners.MaterialSectionListener;

/**
 * Created by Stéphane on 05/05/2015.
 */

public class MainActivity extends MaterialNavigationDrawer implements Target {

    private String  mFirstname;
    private String  mLastname;
    private String  mEmail;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void init(Bundle bundle) {
        MyUtils.setActivity(this);


        MaterialSection sectionRestaurants = newSection("Restaurants", getResources().getDrawable(R.drawable.ic_home_white), new FragmentRestaurants());
        addSection(sectionRestaurants);

        MaterialSection sectionNFCListen = newSection("NFC Reader", getResources().getDrawable(R.drawable.ic_nfc_white) ,new FragmentNFCReader());
        addSection(sectionNFCListen);

        addDivisor();

        MaterialSection sectionLogout = newSection("Log out", getResources().getDrawable(R.drawable.ic_exit_to_app_white), new MaterialSectionListener() {
            @Override
            public void onClick(MaterialSection materialSection) {
                SharedPreferences.Editor editor = getSharedPreferences(AuthentificationActivity.PREFS_NAME, 0).edit();
                editor.putString("access_token", null);
                editor.putString("refresh_token", null);
                editor.commit();
                loadAuthentificationActivity();
            }
        });
        addSection(sectionLogout);

        MaterialAccount account = new MaterialAccount(getResources(),
                "firstname lastname",
                "", null, R.drawable.drawer_header);
        addAccount(account);

        FoodieRestClient.get("me", null, MyUtils.getAccessTokenFromPreferences(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {

                    String urlImageProfile = "http://foodie.dennajort.fr" + response.getString("picture");
                    mFirstname = response.getString("firstname");
                    mLastname = response.getString("lastname");
                    mEmail = response.getString("email");

                    MyUtils.getPicasso().with(MainActivity.this).load(urlImageProfile).into(MainActivity.this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {}
        });
    }

    public void loadAuthentificationActivity() {
        Intent intent = new Intent(MainActivity.this, AuthentificationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        MaterialAccount account = new MaterialAccount(getResources(),
                mFirstname + " " + mLastname, mEmail, null, bitmap);
        addAccount(account);
        removeAccount(getCurrentAccount());
        notifyAccountDataChanged();
    }

    @Override
    public void onBitmapFailed(Drawable errorDrawable) {
        MaterialAccount account = new MaterialAccount(getResources(),
                mFirstname + " " + mLastname, mEmail, null, R.drawable.drawer_header);
        addAccount(account);
        removeAccount(getCurrentAccount());
        notifyAccountDataChanged();
    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {

    }
}
