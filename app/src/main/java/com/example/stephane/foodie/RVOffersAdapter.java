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

import com.afollestad.materialdialogs.MaterialDialog;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONObject;

import java.util.ArrayList;

import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;

/**
 * Created by Stéphane on 06/05/2015.
 */

public class RVOffersAdapter extends RecyclerView.Adapter<RVOffersAdapter.OfferViewHolder>{

    ArrayList<Offer>            mOffers;
    MaterialNavigationDrawer    mActivity;

    RVOffersAdapter(MaterialNavigationDrawer activity, ArrayList<Offer> offers) {
        mActivity = activity;
        mOffers = offers;
    }

    public static class OfferViewHolder extends RecyclerView.ViewHolder {
        CardView    mCardView;
        TextView    mOfferDesignation;
        TextView    mOfferDescription;
        TextView    mOfferExpirationDate;
        //Button      mButtonEdit;
        Button      mButtonDelete;

        OfferViewHolder(View itemView) {
            super(itemView);
            mCardView = (CardView)itemView.findViewById(R.id.cardView_restaurant);
            mOfferDesignation = (TextView)itemView.findViewById(R.id.textView_offerDesignation);
            mOfferDescription = (TextView)itemView.findViewById(R.id.textView_offerDescription);
            mOfferExpirationDate = (TextView) itemView.findViewById(R.id.textView_offerExpirationDate);
            //mButtonEdit = (Button)itemView.findViewById(R.id.button_edit);
            mButtonDelete = (Button) itemView.findViewById(R.id.button_delete);
/*
            FrameLayout fl = (FrameLayout) itemView.findViewById(R.id.fl_header);
            fl.getLayoutParams().height =  MyUtils.getHeightRatio_16_9(MyUtils.getDisplayMetrics().widthPixels - 32);
            fl.requestLayout();
            */
        }
    }

    @Override
    public OfferViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_offer, viewGroup, false);
        OfferViewHolder rvh = new OfferViewHolder(v);
        return rvh;
    }

    @Override
    public void onBindViewHolder(final OfferViewHolder offerViewHolder, final int i) {
        offerViewHolder.mOfferDesignation.setText(mOffers.get(i).getDesignation());
        offerViewHolder.mOfferDescription.setText(mOffers.get(i).getDescription());
        offerViewHolder.mOfferExpirationDate.setText(mOffers.get(i).getExpirationDate());
        offerViewHolder.mButtonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new MaterialDialog.Builder(mActivity)
                        .title("Delete an offer")
                        .content("The selected offer will be removed.")
                        .positiveText("Confirm")
                        .negativeText("Cancel")
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                FoodieRestClient.delete("me/offers/" + mOffers.get(i).getId(), MyUtils.getAccessTokenFromPreferences(), new JsonHttpResponseHandler() {
                                    @Override
                                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                        mOffers.remove(i);
                                        notifyDataSetChanged();
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
        return mOffers.size();
    }

}