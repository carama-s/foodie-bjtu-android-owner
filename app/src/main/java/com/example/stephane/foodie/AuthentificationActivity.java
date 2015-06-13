package com.example.stephane.foodie;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

public class AuthentificationActivity extends ActionBarActivity {

    public static final String      PREFS_NAME = "FoodiePreferencesFile";
    private static int              SPLASH_TIME_OUT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentification);
        MyUtils.setActivity(this);

        final SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String accessToken = preferences.getString("access_token", null);
        String refreshToken = preferences.getString("refresh_token", null);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().hide();

        if (accessToken == null && refreshToken == null) {
            FragmentLogin loginFragment = new FragmentLogin();
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, loginFragment).commit();
        } else {
            RequestParams params = new RequestParams();
            params.add("grant_type", "refresh_token");
            params.add("refresh_token", refreshToken);

            FoodieRestClient.postOauth("access_token", params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Log.e("access_token_refresh", "onSuccess: " + response);
                    SharedPreferences.Editor editor = preferences.edit();
                    try {
                        editor.putString("access_token", response.getString("access_token"));
                        editor.putString("refresh_token", response.getString("refresh_token"));
                        editor.commit();
                        loadMainActivity();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Log.e("access_token_refresh", "onFailure: " + errorResponse);
                    FragmentLogin loginFragment = new FragmentLogin();
                    getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, loginFragment).commit();
                }
            });
        }

        /*
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                if (findViewById(R.id.fragment_container) != null) {

                    FragmentLogin loginFragment = new FragmentLogin();

                    getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, loginFragment).commit();
                }
            }
        }, SPLASH_TIME_OUT);
        */
    }

    public void loadMainActivity() {
        Intent intent = new Intent(AuthentificationActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
    }

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
}
