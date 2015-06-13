package com.example.stephane.foodie;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
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
import android.widget.ScrollView;
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

import org.apache.http.Header;
import org.json.JSONObject;
import org.lucasr.twowayview.ItemClickSupport;
import org.lucasr.twowayview.widget.TwoWayView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Stéphane on 25/04/2015.
 */

public class FragmentAddRestaurant extends Fragment implements OnMapReadyCallback {

    private GoogleMap               mGoogleMap;
    private MaterialEditText        mNameField;
    private MaterialEditText        mDescriptionField;
    private MaterialEditText        mPhoneField;
    private MaterialEditText        mEmailField;
    private MaterialEditText        mAddressField;
    private Address                 mAddressGM = null;

    private ImageView               mImagePresentation;
    private Uri                     mImagePresentationUri;
    private FloatingActionsMenu     mFabAddImage;
    private FloatingActionButton    mFabAddHeaderRestaurant;

    private TwoWayView              mRecyclerView;
    private RVGalleryAdapter        mAdapterRecyclerView;
    private LinearLayout            mLl_gallery;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View content = inflater.inflate(R.layout.fragment_add_restaurant, container, false);

        // Init map
        SupportMapFragment m = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.fragment_map));
        m.getMapAsync(this);

        mFabAddHeaderRestaurant = (FloatingActionButton) content.findViewById(R.id.fab_addImageHeaderRestaurant);
        mFabAddHeaderRestaurant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, 100);
            }
        });

        FloatingActionButton fabAddImageGallery = (FloatingActionButton) content.findViewById(R.id.fab_addImageGallery);
        fabAddImageGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, 101);
            }
        });

        mFabAddImage = (FloatingActionsMenu) content.findViewById(R.id.fab_imageRestaurant);

        init_layouts(content);
        init_components(content);
        init_search_address();

        mRecyclerView = (TwoWayView) content.findViewById(R.id.twowayview_imageGallery);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLongClickable(true);

        mAdapterRecyclerView = new RVGalleryAdapter(getActivity(), mRecyclerView);
        mRecyclerView.setAdapter(mAdapterRecyclerView);



        final ItemClickSupport itemClick = ItemClickSupport.addTo(mRecyclerView);

        itemClick.setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView parent, View child, int position, long id) {

            }
        });

        itemClick.setOnItemLongClickListener(new ItemClickSupport.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(RecyclerView parent, View child, int position, long id) {
                mAdapterRecyclerView.removeItem(position);
                if (mAdapterRecyclerView.getItemCount() == 0) {
                    mLl_gallery.setVisibility(View.GONE);
                }
                return true;
            }
        });

        return content;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mFabAddImage.collapse();
        switch(requestCode) {
            case 100:
                if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
                    mImagePresentationUri = data.getData();

                    mFabAddHeaderRestaurant.setColorNormal(getResources().getColor(R.color.colorAccent));
                    mFabAddHeaderRestaurant.setColorPressed(getResources().getColor(R.color.colorAccent));

                    mImagePresentation.setImageURI(mImagePresentationUri);
                }
            case 101:
                if (requestCode == 101 && resultCode == Activity.RESULT_OK) {
                    Uri selectedImage = data.getData();
                    if (mAdapterRecyclerView.getItemCount() == 0) {
                        mLl_gallery.setVisibility(View.VISIBLE);
                    }
                    mAdapterRecyclerView.addItem(new ImageGallery(selectedImage));
                }
        }
    }

    private void init_layouts(View content) {
        LinearLayout ll_header = (LinearLayout) content.findViewById(R.id.ll_header);
        ll_header.getLayoutParams().height = MyUtils.getHeightRatio_16_9();
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

        mImagePresentation = (ImageView) content.findViewById(R.id.imageView_imagePresentation);

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

    public void init_search_address() {
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
                }, 1000);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.getUiSettings().setScrollGesturesEnabled(false);
    }

    @Override
    public void onCreateOptionsMenu( Menu menu, MenuInflater inflater ) {
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem settingsItem = menu.findItem(R.id.action_settings);
        settingsItem.setVisible(false);

        inflater.inflate(R.menu.menu_add_restaurant, menu);

        MenuItem menuItemValidate = menu.findItem(R.id.action_validate);
        menuItemValidate.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                validateAddRestaurant();
                return true;
            }
        });
    }

    public void validateAddRestaurant() {
        boolean vName = mNameField.validate();
        boolean vDescription = mDescriptionField.validate();
        boolean vPhone = mPhoneField.validate();
        boolean vEmail = mEmailField.validate();

        if (mImagePresentation.getDrawable() == null) {
            mFabAddImage.expand();
            mFabAddHeaderRestaurant.setColorNormal(getResources().getColor(R.color.colorError));
            mFabAddHeaderRestaurant.setColorPressed(getResources().getColor(R.color.colorError));
        }

        MyUtils.hideKeyboard();

        if (vName && vDescription && vPhone && vEmail && mAddressGM != null) {

            RequestParams params = new RequestParams();
            params.add("name", mNameField.getText().toString());
            params.add("short_description", mDescriptionField.getText().toString());
            params.add("address", MyUtils.getAddressLine(mAddressGM));
            params.add("email", mEmailField.getText().toString());
            params.add("phone", mPhoneField.getText().toString());
            params.put("longitude", mAddressGM.getLongitude());
            params.put("latitude", mAddressGM.getLatitude());

            FoodieRestClient.post("me/restaurants", params, MyUtils.getAccessTokenFromPreferences(), new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                    RequestParams params = new RequestParams();
                    String id = null;
                    try {
                        id = response.getString("id");
                        params.put("file", new File(MyUtils.getPathFromMediaUri(getActivity(), mImagePresentationUri)));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    final String idRestaurant = id;
                    FoodieRestClient.post("me/restaurants/" + id + "/main_picture", params, MyUtils.getAccessTokenFromPreferences(), new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            Toast.makeText(getActivity(), "success upload main image", Toast.LENGTH_SHORT).show();
                            if (mAdapterRecyclerView.getGallery().size() == 0) {
                                getActivity().onBackPressed();
                            } else {
                                addImageGallery(idRestaurant, mAdapterRecyclerView.getGallery());
                            }
                        }
                    });
                }
            });
        }
    }

    public void addImageGallery(final String restaurantId, final List<ImageGallery> gallery) {
        if (gallery.size() == 0) {
            Toast.makeText(getActivity(), "success upload gallery", Toast.LENGTH_LONG).show();
            getActivity().onBackPressed();
            return;
        }

        RequestParams params = new RequestParams();
        try {
            params.put("file", new File(MyUtils.getPathFromMediaUri(getActivity(), gallery.get(0).getUri())));
        } catch (Exception e) {
            e.printStackTrace();
        }

        FoodieRestClient.post("me/restaurants/" + restaurantId + "/gallery", params, MyUtils.getAccessTokenFromPreferences(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                gallery.remove(0);
                addImageGallery(restaurantId, gallery);
            }
        });
    }

}
