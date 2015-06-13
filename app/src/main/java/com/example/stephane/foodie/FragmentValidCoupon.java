package com.example.stephane.foodie;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONObject;

/**
 * Created by Stéphane on 10/05/2015.
 */
public class FragmentValidCoupon extends Fragment {

    private Restaurant  mRestaurant;
    private Offer       mOffer;
    private String      mSecret;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mRestaurant = getArguments().getParcelable("restaurantParcelable");
        mOffer = getArguments().getParcelable("offerParcelable");
        mSecret = getArguments().getString("secret");
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View content = inflater.inflate(R.layout.fragment_valid_coupon, container, false);

        init_layouts(content);
        init_components(content);


        return content;
    }

    private void init_layouts(View content) {
        FrameLayout fl = (FrameLayout) content.findViewById(R.id.fl_header);
        fl.getLayoutParams().height =  MyUtils.getHeightRatio_16_9(MyUtils.getDisplayMetrics().widthPixels - 32);
        fl.requestLayout();
    }

    private void init_components(View content) {

        final ImageView couponImage = (ImageView) content.findViewById(R.id.imageView_image);
        mRestaurant.onMainPictureInfo(new Restaurant.onMainPictureCallback() {
            @Override
            public void onInfo(RestaurantImage image) {
                Picasso.with(getActivity()).load(image.getFullUrl()).into(couponImage);
            }
        });

        TextView textDescriptionOffer = (TextView) content.findViewById(R.id.textView_offerDescription);
        textDescriptionOffer.setText(mOffer.getDescription());

        Button buttonCancel = (Button) content.findViewById(R.id.button_cancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        Button buttonValid = (Button) content.findViewById(R.id.button_valid);
        buttonValid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                RequestParams params = new RequestParams();
                params.put("secret", mSecret);
                FoodieRestClient.put("me/coupons/use", params, MyUtils.getAccessTokenFromPreferences(), new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        getActivity().onBackPressed();
                    }
                });
            }
        });


    }
}