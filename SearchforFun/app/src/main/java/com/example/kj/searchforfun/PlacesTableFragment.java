package com.example.kj.searchforfun;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



public class PlacesTableFragment extends Fragment {
    public static RecyclerView mRecyclerView;
    private TextView mEmptyView;
    public RecycleAdapter mAdapter;
    public static RecycleAdapter searchAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private static JSONArray jsonArr;

    public static PlacesTableFragment newInstance(RecycleAdapter searchAdapter) {
        PlacesTableFragment placesTableFragment = new PlacesTableFragment();
        placesTableFragment.searchAdapter = searchAdapter;
        return placesTableFragment;
    }

    public static PlacesTableFragment newInstance() {
        PlacesTableFragment placesTableFragment = new PlacesTableFragment();
        placesTableFragment.mAdapter = MainActivity.mRecycleAdapter;
        return placesTableFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View frag = inflater.inflate(R.layout.places_table, container, false);
        mRecyclerView = (RecyclerView) frag.findViewById(R.id.my_recycler_view);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        if(mAdapter != null) {
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mRecyclerView.setAdapter(searchAdapter);
        }
        return frag;
    }
}
