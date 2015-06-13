package com.example.stephane.foodie;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;

/**
 * Created by Stéphane on 10/05/2015.
 */

public class FragmentNFCReader extends Fragment implements CouponReaderCard.CouponCallback {

    // Recommend NfcAdapter flags for reading from other Android devices. Indicates that this
    // activity is interested in NFC-A devices (including other Android devices), and that the
    // system should not check for the presence of NDEF-formatted data (e.g. Android Beam).
    public static int           READER_FLAGS = NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK;
    public CouponReaderCard     mCouponCardReader;
    private TextView            mStateNFC;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View content = inflater.inflate(R.layout.fragment_nfc_reader, container, false);

        init_components(content);

        return content;
    }

    private void init_components(View content) {

        mStateNFC = (TextView) content.findViewById(R.id.textView_stateNFC);
        mStateNFC.setText("Waiting for device");
        mCouponCardReader = new CouponReaderCard(this);
        enableReaderMode();
    }

    @Override
    public void onPause() {
        super.onPause();
        disableReaderMode();
    }

    @Override
    public void onResume() {
        super.onResume();
        enableReaderMode();
    }

    private void enableReaderMode() {
        Log.i("enableReaderMode", "Enabling reader mode");
        Activity activity = getActivity();
        NfcAdapter nfc = NfcAdapter.getDefaultAdapter(activity);
        if (!nfc.isEnabled()) {
            Toast.makeText(getActivity(), "Please enable NFC.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
        }
        if (nfc != null) {
            nfc.enableReaderMode(activity, mCouponCardReader, READER_FLAGS, null);
        }
    }

    private void disableReaderMode() {
        Log.i("disableReaderMode", "Disabling reader mode");
        Activity activity = getActivity();
        NfcAdapter nfc = NfcAdapter.getDefaultAdapter(activity);
        if (nfc != null) {
            nfc.disableReaderMode(activity);
        }
    }

    @Override
    public void onCouponReceived(final String secretCode) {

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                RequestParams params = new RequestParams();

                params.put("secret", secretCode);

                mStateNFC.setText("Checking");

                FoodieRestClient.get("me/coupons/check", params, MyUtils.getAccessTokenFromPreferences(), new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                        try {
                            boolean valid = response.getBoolean("valid");

                            if (valid) {
                                mStateNFC.setText("Getting coupon");
                                Restaurant restaurant = new Restaurant(response.getJSONObject("restaurant"));
                                Offer offer = new Offer(response.getJSONObject("offer"));

                                Fragment fragment = new FragmentValidCoupon();
                                Bundle args = new Bundle();
                                args.putParcelable("restaurantParcelable", restaurant);
                                args.putParcelable("offerParcelable", offer);
                                args.putString("secret", secretCode);
                                fragment.setArguments(args);

                                MaterialNavigationDrawer a = (MaterialNavigationDrawer) FragmentNFCReader.this.getActivity();
                                a.setFragmentChild(fragment, "");
                            } else {
                                mStateNFC.setText("This coupon is not valid !");
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        Log.e("here", "onFailure: " + errorResponse);
                    }
                });
            }
        });
    }
}
