package com.example.stephane.foodie;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Stéphane on 01/05/2015.
 */

public class FragmentLogin extends Fragment {

    private MaterialEditText    mAddressMailField;
    private MaterialEditText    mPasswordField;
    private Button              mLoginButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View content = inflater.inflate(R.layout.fragment_login, container, false);

        Button button_signUp = (Button) content.findViewById(R.id.button_signUp);
        button_signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentCreateAccount createAccountFragment = new FragmentCreateAccount();
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_container, createAccountFragment);
                ft.addToBackStack(null);
                ft.commit();
            }
        });

        mLoginButton = (Button) content.findViewById(R.id.button_login);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RequestParams params = new RequestParams();
                params.add("grant_type", "password");
                params.add("username", mAddressMailField.getText().toString());
                params.add("password", mPasswordField.getText().toString());

                MyUtils.hideKeyboard();

                FoodieRestClient.postOauth("access_token", params, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        Log.e("access_token", "onSuccess: " + response);

                        SharedPreferences.Editor editor = getActivity().getSharedPreferences(AuthentificationActivity.PREFS_NAME, 0).edit();
                        try {
                            editor.putString("access_token", response.getString("access_token"));
                            editor.putString("refresh_token", response.getString("refresh_token"));
                            editor.commit();

                            ((AuthentificationActivity) getActivity()).loadMainActivity();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        Log.e("access_token", "onFailure: " + errorResponse);
                        Toast.makeText(getActivity(), "" + errorResponse, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        mAddressMailField = (MaterialEditText) content.findViewById(R.id.met_addressMail);
        mPasswordField = (MaterialEditText) content.findViewById(R.id.met_password);

        mAddressMailField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                checkLoginButton();
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        mPasswordField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                checkLoginButton();
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });


        return content;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void checkLoginButton() {
        if ((!mAddressMailField.getText().toString().trim().isEmpty()) && (!mPasswordField.getText().toString().trim().isEmpty())) {
            mLoginButton.setEnabled(true);
            mLoginButton.setTextColor(Color.WHITE);
        } else {
            mLoginButton.setEnabled(false);
            mLoginButton.setTextColor(Color.parseColor("#D6D7D7"));
        }
    }
}
