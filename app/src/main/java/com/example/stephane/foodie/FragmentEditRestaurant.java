package com.example.stephane.foodie;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rengwuxian.materialedittext.validation.RegexpValidator;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Stéphane on 03/05/2015.
 */

public class FragmentEditRestaurant extends Fragment implements OnMapReadyCallback {

    private MaterialEditText    mNameField;
    private MaterialEditText    mDescriptionField;
    private MaterialEditText    mPhoneField;
    private MaterialEditText    mEmailField;
    private MaterialEditText    mAddressField;
    private GoogleMap           mGoogleMap;
    private Address             mAddressGM = null;
    private FloatingActionsMenu mFabEditImage;
    private ImageView           mImagePresentation;
    private Restaurant          mRestaurant;
    private Uri                 mImagePresentationUri = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mRestaurant = getArguments().getParcelable("restaurantParcelable");
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View content = inflater.inflate(R.layout.fragment_edit_restaurant, container, false);

        // Init map
        SupportMapFragment m = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.fragment_map));
        m.getMapAsync(this);

        init_layouts(content);
        init_components(content);
        init_search_address();
        fill_fields();

        FloatingActionButton fabEditHeaderRestaurant = (FloatingActionButton) content.findViewById(R.id.fab_editImageHeaderRestaurant);
        fabEditHeaderRestaurant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, 100);
            }
        });

        FloatingActionButton fabEditImageGallery = (FloatingActionButton) content.findViewById(R.id.fab_editImageGallery);
        fabEditImageGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, 101);
            }
        });


        return content;
    }

    private void init_layouts(View content) {

        LinearLayout ll_header = (LinearLayout) content.findViewById(R.id.ll_header);
        ll_header.getLayoutParams().height = (MyUtils.getHeightRatio_16_9());
        ll_header.requestLayout();

        LinearLayout ll_map = (LinearLayout) content.findViewById(R.id.ll_map);
        ll_map.getLayoutParams().height = MyUtils.getHeightRatio_4_3(MyUtils.getDisplayMetrics().widthPixels - 72 - 16); // padding left and padding right
        ll_map.requestLayout();
    }

    private void init_components(View content) {

        mFabEditImage = (FloatingActionsMenu) content.findViewById(R.id.fab_imageRestaurant);

        mImagePresentation = (ImageView) content.findViewById(R.id.imageView_imagePresentation);

        mRestaurant.onMainPictureInfo(new Restaurant.onMainPictureCallback() {
            @Override
            public void onInfo(RestaurantImage image) {
                String url = image.getFullUrl();
                if (url != null) {
                    MyUtils.getPicasso().with(getActivity()).load(url).into(mImagePresentation);
                }
            }
        });

        mNameField = (MaterialEditText) content.findViewById(R.id.met_restaurantName);
        mDescriptionField = (MaterialEditText) content.findViewById(R.id.met_restaurantDescription);
        mPhoneField = (MaterialEditText) content.findViewById(R.id.met_restaurantPhone);
        mEmailField = (MaterialEditText) content.findViewById(R.id.met_restaurantEmail);
        mAddressField = (MaterialEditText) content.findViewById(R.id.met_searchRestaurantAddress);

        mNameField.addValidator(new RegexpValidator("The restaurant name is required and cannot be empty.", "^.+$"));

        mDescriptionField.addValidator(new RegexpValidator("The description is required and cannot be empty.", "^.+$"));

        mPhoneField.addValidator(new RegexpValidator("The phone number is required and cannot be empty.", "^.+$"));

        mEmailField.addValidator(new RegexpValidator("The email address is required and cannot be empty.", "^.+$"))
                .addValidator(new RegexpValidator("The email address is not valid.", "^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$"));
    }

    private void fill_fields() {
        FoodieRestClient.get("restaurants/" + mRestaurant.getId(), null, "", new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, final JSONObject response) {
                try {
                    mNameField.setText(response.getString("name"));
                    mDescriptionField.setText(response.getString("short_description"));
                    mPhoneField.setText(response.getString("phone"));
                    mEmailField.setText(response.getString("email"));

                    Thread t = new Thread() {
                        public void run() {
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(new Runnable() {
                                    public void run() {
                                        mGoogleMap.clear();
                                        try {
                                            mAddressGM = MyUtils.findLocationByCoordinates(getActivity(), response.getDouble("latitude"), response.getDouble("longitude"));
                                            if (mAddressGM != null) {
                                                LatLng restaurantAddressPoint = new LatLng(mAddressGM.getLatitude(), mAddressGM.getLongitude());
                                                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(restaurantAddressPoint, 15));

                                                Marker addressMarker = mGoogleMap.addMarker(new MarkerOptions()
                                                        .position(restaurantAddressPoint)
                                                        .title("Restaurant address")
                                                        .snippet(MyUtils.getAddressLine(mAddressGM)));
                                                addressMarker.showInfoWindow();
                                                mAddressField.setText(MyUtils.getAddressLine(mAddressGM));
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

    private void init_search_address() {
        mAddressField.addTextChangedListener(new TextWatcher() {

            private Timer timerStopTyping = new Timer();

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(final Editable s) {
                timerStopTyping.cancel();
                timerStopTyping = new Timer();
                timerStopTyping.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    MyUtils.hideKeyboard();
                                    mGoogleMap.clear();
                                    mAddressGM = null;
                                    List<Address> addresses = MyUtils.findLocationByAddress(getActivity(), s.toString());
                                    if (addresses != null) {
                                        mAddressGM = addresses.get(0);
                                        LatLng restaurantAddressPoint = new LatLng(mAddressGM.getLatitude(), mAddressGM.getLongitude());
                                        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(restaurantAddressPoint, 15));

                                        Marker addressMarker = mGoogleMap.addMarker(new MarkerOptions()
                                                .position(restaurantAddressPoint)
                                                .title("Restaurant address")
                                                .snippet(MyUtils.getAddressLine(mAddressGM)));
                                        addressMarker.showInfoWindow();
                                    }
                                }
                            });
                        }
                    }
                }, 1000);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mFabEditImage.collapse();
        switch(requestCode) {
            case 100:
                if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
                    mImagePresentationUri = data.getData();
                    mImagePresentation.setImageURI(mImagePresentationUri);
                }
            case 101:
                /*
                if (requestCode == 101 && resultCode == Activity.RESULT_OK) {
                    Uri selectedImage = data.getData();
                    if (mAdapterRecyclerView.getItemCount() == 0) {
                        mLl_gallery.setVisibility(View.VISIBLE);
                    }
                    mAdapterRecyclerView.addItem(new ImageGallery(selectedImage));
                }
                */
        }
    }

    @Override
    public void onCreateOptionsMenu( Menu menu, MenuInflater inflater ) {
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem settingsItem = menu.findItem(R.id.action_settings);
        settingsItem.setVisible(false);

        inflater.inflate(R.menu.menu_edit_restaurant, menu);
        MenuItem menuItemValidate = menu.findItem(R.id.action_validate);
        menuItemValidate.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
            validateEditRestaurant();
            return true;
            }
        });
    }

    public void validateEditRestaurant() {
        boolean vName = mNameField.validate();
        boolean vShortDescription = mDescriptionField.validate();
        boolean vPhone = mPhoneField.validate();
        boolean vEmail = mEmailField.validate();

        MyUtils.hideKeyboard();

        if (vName && vShortDescription && vPhone && vEmail && mAddressGM != null) {
            RequestParams params = new RequestParams();
            params.put("name", mNameField.getText().toString());
            params.put("short_description", mDescriptionField.getText().toString());
            params.put("email", mEmailField.getText().toString());
            params.put("phone", mPhoneField.getText().toString());
            params.put("address", MyUtils.getAddressLine(mAddressGM));
            params.put("longitude", mAddressGM.getLongitude());
            params.put("latitude", mAddressGM.getLatitude());

            FoodieRestClient.put("me/restaurants/" + mRestaurant.getId(), params, MyUtils.getAccessTokenFromPreferences(), new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                    if (mImagePresentationUri != null) {

                        RequestParams params = new RequestParams();
                        try {
                            params.put("file", new File(MyUtils.getPathFromMediaUri(getActivity(), mImagePresentationUri)));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        FoodieRestClient.post("me/restaurants/" + mRestaurant.getId() + "/main_picture", params, MyUtils.getAccessTokenFromPreferences(), new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                Toast.makeText(getActivity(), "success edit main image", Toast.LENGTH_SHORT).show();
                                getActivity().onBackPressed();
                            }
                        });
                    } else {
                        getActivity().onBackPressed();
                    }
                }
            });
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
    }
}
