package com.example.stephane.foodie;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.getbase.floatingactionbutton.AddFloatingActionButton;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;

import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;

/**
 * Created by Stéphane on 10/05/2015.
 */
public class FragmentOffersRestaurant extends Fragment {

    private SwipeRefreshLayout  mSwipeRefreshListView;
    private RecyclerView        mRecyclerView;
    private Restaurant          mRestaurant;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRestaurant = getArguments().getParcelable("restaurantParcelable");
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View content = inflater.inflate(R.layout.fragment_offers_restaurant, container, false);

        mRecyclerView = (RecyclerView) content.findViewById(R.id.recyclerView_offer);

        mSwipeRefreshListView = (SwipeRefreshLayout) content.findViewById(R.id.swipeRefresh);
        mSwipeRefreshListView.setColorSchemeColors(getResources().getColor(R.color.colorAccent));

        mSwipeRefreshListView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateDataOffers();
            }
        });

        LinearLayoutManager llm_recycler = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(llm_recycler);
        //mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        updateDataOffers();

        AddFloatingActionButton fabAddOffer = (AddFloatingActionButton) content.findViewById(R.id.fab_addOffer);
        fabAddOffer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Fragment fragment = new FragmentAddOffer();

                Bundle args = new Bundle();
                args.putParcelable("restaurantParcelable", mRestaurant);
                fragment.setArguments(args);

                ((MaterialNavigationDrawer) getActivity()).setFragmentChild(fragment, "");
            }
        });

        return content;
    }

    private void updateDataOffers() {

        FoodieRestClient.get("restaurants/" + mRestaurant.getId() + "/offers", null, "", new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                if (getActivity() != null) {
                    RVOffersAdapter adapter = new RVOffersAdapter((MaterialNavigationDrawer) getActivity(), Offer.fromJson(response));
                    mRecyclerView.setAdapter(adapter);

                    mSwipeRefreshListView.setRefreshing(false);
                }
            }
        });
    }
}
