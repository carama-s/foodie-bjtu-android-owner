package com.example.stephane.foodie;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
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
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rengwuxian.materialedittext.validation.RegexpValidator;

import org.apache.http.Header;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Stéphane on 29/04/2015.
 */

public class FragmentCreateAccount extends Fragment {

    private MaterialEditText    mFirstnameField;
    private MaterialEditText    mLastnameField;
    private MaterialEditText    mAddressMailField;
    private MaterialEditText    mPasswordField;

    private ImageView           mImageProfile;
    private Uri                 mImageProfileUri = null;
    private FloatingActionsMenu mFabAddImage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(null);
        actionBar.show();

        View content = inflater.inflate(R.layout.fragment_create_account, container, false);

        FloatingActionButton fabUploadImage = (FloatingActionButton) content.findViewById(R.id.fab_uploadImageAccount);
        fabUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, 100);
            }
        });

        FloatingActionButton fabTakeImage = (FloatingActionButton) content.findViewById(R.id.fab_takeImageAccount);
        fabTakeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, "IMG_" + timeStamp + ".jpg");

                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                mImageProfileUri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,  mImageProfileUri);

                startActivityForResult(cameraIntent, 101);
            }
        });

        mFabAddImage = (FloatingActionsMenu) content.findViewById(R.id.fab_ImageAccount);

        init_layouts(content);
        init_fields(content);

        return content;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mFabAddImage.collapse();

        switch(requestCode) {
            case 100:
                if (resultCode == Activity.RESULT_OK) {
                    mImageProfileUri = data.getData();
                    mImageProfile.setImageURI(mImageProfileUri);
                }
            case 101:
                if (requestCode == 101 && resultCode == Activity.RESULT_OK) {
                    mImageProfile.setImageURI(mImageProfileUri);
                }
        }
    }

    private void init_layouts(View content) {
        LinearLayout ll_header = (LinearLayout) content.findViewById(R.id.ll_header);
        ll_header.getLayoutParams().height = MyUtils.getHeightRatio_16_9();
        ll_header.requestLayout();
    }

    private void init_fields(View content) {
        mFirstnameField = (MaterialEditText) content.findViewById(R.id.met_firstname);
        mLastnameField = (MaterialEditText) content.findViewById(R.id.met_lastname);
        mAddressMailField = (MaterialEditText) content.findViewById(R.id.met_addressMail);
        mPasswordField = (MaterialEditText) content.findViewById(R.id.met_password);

        mFirstnameField.addValidator(new RegexpValidator("The firstname is required and cannot be empty.", "^.+$"));

        mLastnameField.addValidator(new RegexpValidator("The lastname is required and cannot be empty.", "^.+$"));

        mPasswordField.addValidator(new RegexpValidator("The password is required and cannot be empty.", "^.+$"));

        mAddressMailField.addValidator(new RegexpValidator("The email address is required and cannot be empty.", "^.+$"))
                .addValidator(new RegexpValidator("The email address is not valid.", "^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$"));

        mImageProfile = (ImageView) content.findViewById(R.id.imageView_imageProfile);
    }

    @Override
    public void onCreateOptionsMenu( Menu menu, MenuInflater inflater ) {
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem settingsItem = menu.findItem(R.id.action_settings);
        settingsItem.setVisible(false);

        inflater.inflate(R.menu.menu_create_account, menu);

        MenuItem menuItemValidate = (MenuItem) menu.findItem(R.id.action_validate);
        menuItemValidate.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
            validCreateUser();
            return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            getActivity().onBackPressed();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((ActionBarActivity) getActivity()).getSupportActionBar().hide();
    }

    public void validCreateUser() {
        boolean vFirstname = mFirstnameField.validate();
        boolean vLastname = mLastnameField.validate();
        boolean vAddressMail = mAddressMailField.validate();
        boolean vPassword = mPasswordField.validate();

        MyUtils.hideKeyboard();

        if (vFirstname && vLastname && vAddressMail && vPassword) {
            RequestParams params = new RequestParams();
            params.add("firstname", mFirstnameField.getText().toString());
            params.add("lastname", mLastnameField.getText().toString());
            params.add("email", mAddressMailField.getText().toString());
            params.add("password", mPasswordField.getText().toString());
            Log.e("l", "here 1");
            if (mImageProfileUri != null) {
                try {
                    params.put("picture", new File(MyUtils.getPathFromMediaUri(getActivity(), mImageProfileUri)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            FoodieRestClient.post("users", params, "", new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    getActivity().onBackPressed();
                }
            });
        }

    }
}