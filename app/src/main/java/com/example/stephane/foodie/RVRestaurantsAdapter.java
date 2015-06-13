package com.example.stephane.foodie;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONObject;

import java.util.ArrayList;

import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;

/**
 * Created by Stéphane on 06/05/2015.
 */

public class RVRestaurantsAdapter extends RecyclerView.Adapter<RVRestaurantsAdapter.RestaurantViewHolder>{

    ArrayList<Restaurant>       mRestaurants;
    MaterialNavigationDrawer    mActivity;

    RVRestaurantsAdapter(MaterialNavigationDrawer activity, ArrayList<Restaurant> restaurants) {
        mActivity = activity;
        mRestaurants = restaurants;
    }

    public static class RestaurantViewHolder extends RecyclerView.ViewHolder {
        CardView    mCardView;
        TextView    mRestaurantName;
        ImageView   mRestaurantImage;
        Button      mButtonDetails;
        Button      mButtonEdit;
        Button      mButtonDelete;

        RestaurantViewHolder(View itemView) {
            super(itemView);
            mCardView = (CardView)itemView.findViewById(R.id.cardView_restaurant);
            mRestaurantName = (TextView)itemView.findViewById(R.id.textView_name);
            mRestaurantImage = (ImageView)itemView.findViewById(R.id.imageView_image);
            mButtonDetails = (Button)itemView.findViewById(R.id.button_details);
            mButtonEdit = (Button)itemView.findViewById(R.id.button_edit);
            mButtonDelete = (Button) itemView.findViewById(R.id.button_delete);

            FrameLayout fl = (FrameLayout) itemView.findViewById(R.id.fl_header);
            fl.getLayoutParams().height =  MyUtils.getHeightRatio_16_9(MyUtils.getDisplayMetrics().widthPixels - 32);
            fl.requestLayout();
        }
    }

    @Override
    public RestaurantViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_restaurant, viewGroup, false);
        RestaurantViewHolder rvh = new RestaurantViewHolder(v);
        return rvh;
    }

    @Override
    public void onBindViewHolder(final RestaurantViewHolder restaurantViewHolder, final int i) {
        restaurantViewHolder.mRestaurantName.setText(mRestaurants.get(i).getName());


        mRestaurants.get(i).onMainPictureInfo(new Restaurant.onMainPictureCallback() {
            @Override
            public void onInfo(RestaurantImage image) {
                String url = image.getFullUrl();
                if (url != null) {
                    MyUtils.getPicasso().load(url).into(restaurantViewHolder.mRestaurantImage);
                }
            }
        }, true);

        restaurantViewHolder.mRestaurantImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Fragment fragment = new FragmentDetailsRestaurant();
                Bundle args = new Bundle();
                args.putParcelable("restaurantParcelable", mRestaurants.get(i));
                fragment.setArguments(args);

                mActivity.setFragmentChild(fragment, "");
            }
        });

        restaurantViewHolder.mButtonDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new FragmentDetailsRestaurant();
                Bundle args = new Bundle();
                args.putParcelable("restaurantParcelable", mRestaurants.get(i));
                fragment.setArguments(args);

                mActivity.setFragmentChild(fragment, "");
            }
        });

        restaurantViewHolder.mButtonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new FragmentEditRestaurant();
                Bundle args = new Bundle();
                args.putParcelable("restaurantParcelable", mRestaurants.get(i));
                fragment.setArguments(args);

                mActivity.setFragmentChild(fragment, "");
            }
        });

        restaurantViewHolder.mButtonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new MaterialDialog.Builder(mActivity)
                        .title("Delete a restaurant")
                        .content("The following restaurant will be removed: " + mRestaurants.get(i).getName() + ".")
                        .positiveText("Confirm")
                        .negativeText("Cancel")
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                FoodieRestClient.delete("me/restaurants/" + mRestaurants.get(i).getId(), MyUtils.getAccessTokenFromPreferences(), new JsonHttpResponseHandler() {
                                    @Override
                                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                        mRestaurants.remove(i);
                                        notifyDataSetChanged();
                                    }

                                    @Override
                                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                                    }
                                });
                            }

                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return mRestaurants.size();
    }

}