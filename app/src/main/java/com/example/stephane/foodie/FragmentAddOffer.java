package com.example.stephane.foodie;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rengwuxian.materialedittext.validation.RegexpValidator;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.apache.http.Header;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Created by Stéphane on 10/05/2015.
 */
public class FragmentAddOffer extends Fragment implements DatePickerDialog.OnDateSetListener {

    private Restaurant          mRestaurant;
    private MaterialEditText    mDesignationField;
    private MaterialEditText    mDescriptionField;
    private MaterialEditText    mExpirationDateField;
    private String              mExpirationDateValue = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mRestaurant = getArguments().getParcelable("restaurantParcelable");
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View content = inflater.inflate(R.layout.fragment_add_offer, container, false);

        init_layouts(content);
        init_components(content);

        return content;
    }

    private void init_layouts(View content) {
        LinearLayout ll_header = (LinearLayout) content.findViewById(R.id.ll_header);
        ll_header.getLayoutParams().height = MyUtils.getHeightRatio_16_9();
        ll_header.requestLayout();
    }

    private void init_components(final View content) {

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

        mDesignationField = (MaterialEditText) content.findViewById(R.id.met_offerDesignation);
        mDesignationField.addValidator(new RegexpValidator("The offer designation is required and cannot be empty.", "^.+$"));

        mDescriptionField = (MaterialEditText) content.findViewById(R.id.met_offerDescription);
        mDescriptionField.addValidator(new RegexpValidator("The offer description is required and cannot be empty.", "^.+$"));

        mExpirationDateField = (MaterialEditText) content.findViewById(R.id.met_offerExpirationDate);
        mExpirationDateField.addValidator(new RegexpValidator("The offer expiration date is required and cannot be empty.", "^.+$"));
        mExpirationDateField.setInputType(InputType.TYPE_NULL);

        mExpirationDateField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker();
            }
        });

        mExpirationDateField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) { showDatePicker(); }
            }
        });

    }

    private void showDatePicker() {
        Calendar now = Calendar.getInstance();
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                FragmentAddOffer.this,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );
        dpd.setMinDate(Calendar.getInstance());
        dpd.show(getActivity().getFragmentManager(), "Expiration date");
    }

    @Override
    public void onCreateOptionsMenu( Menu menu, MenuInflater inflater ) {
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem settingsItem = menu.findItem(R.id.action_settings);
        settingsItem.setVisible(false);

        inflater.inflate(R.menu.menu_add_offer, menu);

        MenuItem menuItemValidate = menu.findItem(R.id.action_validate);
        menuItemValidate.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                validateAddCoupon();
                return true;
            }
        });
    }

    private void validateAddCoupon() {
        boolean vDesignation = mDesignationField.validate();
        boolean vDescription = mDescriptionField.validate();
        boolean vExpirationDate = mExpirationDateField.validate();

        MyUtils.hideKeyboard();

        if (vDesignation && vDescription && vExpirationDate) {
            RequestParams params = new RequestParams();

            params.put("name", mDesignationField.getText().toString());
            params.put("description", mDescriptionField.getText().toString());
            params.put("expiration_date", mExpirationDateValue);

            FoodieRestClient.post("me/restaurants/" + mRestaurant.getId() + "/offers", params, MyUtils.getAccessTokenFromPreferences(), new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    getActivity().onBackPressed();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Log.e("here", "onFailure: " + errorResponse);
                }
            });

        }
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        mExpirationDateField.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
        SimpleDateFormat spf = new SimpleDateFormat("yyyy-MM-dd");
        GregorianCalendar gc = new GregorianCalendar(year, monthOfYear, dayOfMonth);
        spf.setCalendar(gc);
        mExpirationDateValue = spf.format(gc.getTime());
        Log.e("date", mExpirationDateValue);
    }
}
