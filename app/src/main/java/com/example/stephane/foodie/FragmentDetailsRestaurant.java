package com.example.stephane.foodie;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;
import org.lucasr.twowayview.widget.TwoWayView;

import java.util.ArrayList;

import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;

/**
 * Created by Stéphane on 07/05/2015.
 */

public class FragmentDetailsRestaurant extends Fragment implements OnMapReadyCallback {

    private Restaurant              mRestaurant;
    private GoogleMap               mGoogleMap;
    private TwoWayView              mRecyclerView;
    private RVGalleryAdapter        mAdapterRecyclerView;
    private LinearLayout            mLl_gallery;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRestaurant = getArguments().getParcelable("restaurantParcelable");
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View content = inflater.inflate(R.layout.fragment_details_restaurant, container, false);

        // Init map
        SupportMapFragment m = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.fragment_map));
        m.getMapAsync(this);

        init_layouts(content);
        init_components(content);
        fill_fields(content);
        return content;
    }


    private void init_layouts(View content) {

        FrameLayout ll_header = (FrameLayout) content.findViewById(R.id.ll_header);
        ll_header.getLayoutParams().height = (MyUtils.getHeightRatio_16_9());
        ll_header.requestLayout();

        mLl_gallery = (LinearLayout) content.findViewById(R.id.ll_gallery);
        mLl_gallery.getLayoutParams().height = MyUtils.getHeightImageGallery(MyUtils.getSpanImageGallery());
        mLl_gallery.requestLayout();
        mLl_gallery.setVisibility(View.GONE);

        LinearLayout ll_map = (LinearLayout) content.findViewById(R.id.ll_map);
        ll_map.getLayoutParams().height = MyUtils.getHeightRatio_4_3(MyUtils.getDisplayMetrics().widthPixels - 72 - 16); // padding left and padding right
        ll_map.requestLayout();
    }

    private void init_components(View content) {

        ((ImageView) content.findViewById(R.id.imageView_iconDescription)).setColorFilter(getResources().getColor(R.color.textColorSecondary));
        ((ImageView) content.findViewById(R.id.imageView_iconPhone)).setColorFilter(getResources().getColor(R.color.textColorSecondary));
        ((ImageView) content.findViewById(R.id.imageView_iconMail)).setColorFilter(getResources().getColor(R.color.textColorSecondary));
        ((ImageView) content.findViewById(R.id.imageView_iconAddress)).setColorFilter(getResources().getColor(R.color.textColorSecondary));

        final ImageView imagePresentation = (ImageView) content.findViewById(R.id.imageView_imagePresentation);

        mRestaurant.onMainPictureInfo(new Restaurant.onMainPictureCallback() {
            @Override
            public void onInfo(RestaurantImage image) {
                String url = image.getFullUrl();
                if (url != null) {
                    MyUtils.getPicasso().with(getActivity()).load(url).into(imagePresentation);
                }
            }
        });

        mRecyclerView = (TwoWayView) content.findViewById(R.id.twowayview_imageGallery);
        mRecyclerView.setLongClickable(true);

        mAdapterRecyclerView = new RVGalleryAdapter(getActivity(), mRecyclerView);
        mRecyclerView.setAdapter(mAdapterRecyclerView);

        mRestaurant.onGalleryInfo(new Restaurant.onGalleryCallback() {
            @Override
            public void onInfo(ArrayList<RestaurantImage> images) {

                if (images.size() > 0) {
                    mLl_gallery.setVisibility(View.VISIBLE);
                }
                for (int i = 0; i < images.size(); i++) {
                    mAdapterRecyclerView.addItem(new ImageGallery(images.get(i).getFullUrl()));
                }
            }
        });

        FloatingActionButton fabCoupon = (FloatingActionButton) content.findViewById(R.id.fab_coupon);
        fabCoupon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new FragmentOffersRestaurant();
                Bundle args = new Bundle();
                args.putParcelable("restaurantParcelable", mRestaurant);
                fragment.setArguments(args);

                MaterialNavigationDrawer a = (MaterialNavigationDrawer) FragmentDetailsRestaurant.this.getActivity();
                a.setFragmentChild(fragment, "");
            }
        });
    }

    void fill_fields(final View content) {
        FoodieRestClient.get("restaurants/" + mRestaurant.getId(), null, "", new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, final JSONObject response) {
                try {
                    ((TextView) content.findViewById(R.id.textView_restaurantName)).setText(response.getString("name"));
                    ((TextView) content.findViewById(R.id.textView_restaurantDescription)).setText(response.getString("short_description"));
                    ((TextView) content.findViewById(R.id.textView_restaurantPhone)).setText(response.getString("phone"));
                    ((TextView) content.findViewById(R.id.textView_restaurantMail)).setText(response.getString("email"));

                    Thread t = new Thread() {
                        public void run() {
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(new Runnable() {
                                    public void run() {
                                        mGoogleMap.clear();
                                        try {
                                            Address address = MyUtils.findLocationByCoordinates(getActivity(), response.getDouble("latitude"), response.getDouble("longitude"));
                                            if (address != null) {
                                                LatLng restaurantAddressPoint = new LatLng(address.getLatitude(), address.getLongitude());
                                                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(restaurantAddressPoint, 15));

                                                Marker addressMarker = mGoogleMap.addMarker(new MarkerOptions()
                                                        .position(restaurantAddressPoint)
                                                        .title("Restaurant address")
                                                        .snippet(MyUtils.getAddressLine(address)));
                                                addressMarker.showInfoWindow();
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }
                        }
                    };
                    t.start();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.e("getRestaurantsByID", "onError: " + errorResponse);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
    }
}
