package com.example.stephane.foodie;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.getbase.floatingactionbutton.AddFloatingActionButton;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;
import org.lucasr.twowayview.TwoWayLayoutManager;

import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;

/**
 * Created by Stéphane on 06/05/2015.
 */

public class FragmentRestaurants extends Fragment {

    SwipeRefreshLayout  mSwipeRefreshListView;
    RecyclerView        mRecyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View content = inflater.inflate(R.layout.fragment_restaurants, container, false);

        mRecyclerView = (RecyclerView) content.findViewById(R.id.recyclerView_restaurant);

        mSwipeRefreshListView = (SwipeRefreshLayout) content.findViewById(R.id.swipeRefresh);
        mSwipeRefreshListView.setColorSchemeColors(getResources().getColor(R.color.colorAccent));

        mSwipeRefreshListView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateDataRestaurants();
            }
        });


        LinearLayoutManager llm_recycler = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(llm_recycler);
        //mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        updateDataRestaurants();

        AddFloatingActionButton fabAddRestaurant = (AddFloatingActionButton) content.findViewById(R.id.fab_addRestaurant);
        fabAddRestaurant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new FragmentAddRestaurant();
                ((MaterialNavigationDrawer) getActivity()).setFragmentChild(fragment, "");
            }
        });

        return content;
    }

    private void updateDataRestaurants() {

        FoodieRestClient.get("me/restaurants", null, MyUtils.getAccessTokenFromPreferences(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Log.e("getRestaurantsByID", "onSuccess: " + response);
                if (getActivity() != null) {
                    RVRestaurantsAdapter adapter = new RVRestaurantsAdapter((MaterialNavigationDrawer) getActivity(), Restaurant.fromJson(response));
                    mRecyclerView.setAdapter(adapter);
                    mSwipeRefreshListView.setRefreshing(false);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.e("getRestaurantsByID", "onError: " + errorResponse);
            }
        });
    }
}
