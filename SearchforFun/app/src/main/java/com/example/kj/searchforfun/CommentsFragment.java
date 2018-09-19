package com.example.kj.searchforfun;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CommentsFragment extends Fragment {
    private View frag;
    private CommentsRecyclerAdapter googleRecyclerAdapter;
    private CommentsRecyclerAdapter yelpRecyclerAdapter;
    private JSONObject jsonObj;
    private JSONArray jsonResponse;
    private JSONArray jsonArr;
    private JSONArray initGoogle;
    private JSONArray initYelp;
    private ArrayList<JSONObject> jsonListGoogle;
    private ArrayList<JSONObject> jsonListYelp;
    private RecyclerView commentsView;
    private Spinner reviews;
    private String[] reviewsArr = {"Google reviews", "Yelp reviews"};
    private Spinner sorting;
    private String[] sortingArr = {"Default order", "Highest rating", "Lowest rating", "Most recent", "Least recent"};
    private String name;
    private String address1;
    private String city;
    private String state;
    private String country;
    private RequestQueue queue;

    public static CommentsFragment newInstance(JSONObject jsonObject) {
        CommentsFragment commentsFragment = new CommentsFragment();
        commentsFragment.jsonObj = jsonObject;
        return commentsFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        frag = inflater.inflate(R.layout.comment_tab, container, false);

        init();
        //setCommentsAdatpter();
        return frag;
    }

    private void init() {
        reviews = (Spinner) frag.findViewById(R.id.reviews);
        sorting = (Spinner) frag.findViewById(R.id.sorting);
        commentsView = (RecyclerView) frag.findViewById(R.id.comments_recycler_view);
        createSpinner(reviews, reviewsArr);
        createSpinner(sorting, sortingArr);
        jsonListGoogle = new ArrayList<JSONObject>();
        jsonListYelp = new ArrayList<JSONObject>();
        try{
            jsonArr = jsonObj.getJSONArray("reviews");
        } catch (JSONException e) {
        }
        queue = Volley.newRequestQueue(getActivity());
        getYelpReviews();
        commentsView.setLayoutManager(new LinearLayoutManager(getActivity()));
        reviews.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position==0){
                    getJsonList(jsonListGoogle, jsonArr);
                    googleRecyclerAdapter = new CommentsRecyclerAdapter(jsonArr);
                    commentsView.setAdapter(googleRecyclerAdapter);
                }else{
                    getJsonList(jsonListYelp, jsonResponse);
                    yelpRecyclerAdapter = new CommentsRecyclerAdapter(jsonResponse);
                    commentsView.setAdapter(yelpRecyclerAdapter);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        sorting.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0) {
                    try{
                        jsonArr = jsonObj.getJSONArray("reviews");
                    } catch (JSONException e) {
                    }
                    jsonResponse = initYelp;
                    if(reviews.getSelectedItem().toString() == "Google reviews") {
                        googleRecyclerAdapter.mDataset = jsonArr;
                        googleRecyclerAdapter.notifyDataSetChanged();
                    } else {
                        yelpRecyclerAdapter.mDataset = jsonResponse;
                        yelpRecyclerAdapter.notifyDataSetChanged();
                    }
                } else if(position == 1) {
                    highestRating(jsonListGoogle);
                    highestRating(jsonListYelp);
                    if(reviews.getSelectedItem().toString() == "Google reviews") {
                        jsonArr = new JSONArray(jsonListGoogle);
                        googleRecyclerAdapter.mDataset = jsonArr;
                        googleRecyclerAdapter.notifyDataSetChanged();
                    } else {
                        jsonResponse = new JSONArray(jsonListYelp);
                        yelpRecyclerAdapter.mDataset = jsonResponse;
                        yelpRecyclerAdapter.notifyDataSetChanged();
                    }
                }else if(position == 2) {
                    lowestRating(jsonListGoogle);
                    lowestRating(jsonListYelp);
                    if(reviews.getSelectedItem().toString() == "Google reviews") {
                        jsonArr = new JSONArray(jsonListGoogle);
                        googleRecyclerAdapter.mDataset = jsonArr;
                        googleRecyclerAdapter.notifyDataSetChanged();
                    } else {
                        jsonResponse = new JSONArray(jsonListYelp);
                        yelpRecyclerAdapter.mDataset = jsonResponse;
                        yelpRecyclerAdapter.notifyDataSetChanged();
                    }
                }else if(position == 3) {
                    mostRecent(jsonListGoogle);
                    mostRecent(jsonListYelp);
                    if(reviews.getSelectedItem().toString() == "Google reviews") {
                        jsonArr = new JSONArray(jsonListGoogle);
                        googleRecyclerAdapter.mDataset = jsonArr;
                        googleRecyclerAdapter.notifyDataSetChanged();
                    } else {
                        jsonResponse = new JSONArray(jsonListYelp);
                        yelpRecyclerAdapter.mDataset = jsonResponse;
                        yelpRecyclerAdapter.notifyDataSetChanged();
                    }
                }else if(position == 4) {
                    leastRecent(jsonListGoogle);
                    leastRecent(jsonListYelp);
                    if(reviews.getSelectedItem().toString() == "Google reviews") {
                        jsonArr = new JSONArray(jsonListGoogle);
                        googleRecyclerAdapter.mDataset = jsonArr;
                        googleRecyclerAdapter.notifyDataSetChanged();
                    } else {
                        jsonResponse = new JSONArray(jsonListYelp);
                        yelpRecyclerAdapter.mDataset = jsonResponse;
                        yelpRecyclerAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void highestRating(ArrayList<JSONObject> jsonList) {
        Collections.sort(jsonList, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject a, JSONObject b) {
                String valA = new String();
                String valB = new String();
                try {
                    valA = (String)a.getString("rating");
                    valB = (String)b.getString("rating");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                return valB.compareTo(valA);
            }
        });
    }

    private void lowestRating(ArrayList<JSONObject> jsonList) {
        Collections.sort(jsonList, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject a, JSONObject b) {
                String valA = new String();
                String valB = new String();
                try {
                    valA = (String)a.getString("rating");
                    valB = (String)b.getString("rating");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                return valA.compareTo(valB);
            }
        });
    }

    private void mostRecent(ArrayList<JSONObject> jsonList) {
        Collections.sort(jsonList, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject a, JSONObject b) {
                String valA = new String();
                String valB = new String();
                try {
                    if(a.has("time_created")) {
                        valA = (String) a.getString("time_created");
                        valB = (String) b.getString("time_created");
                    } else {
                        valA = (String)a.getString("time");
                        valB = (String)b.getString("time");
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                return valB.compareTo(valA);
            }
        });
    }

    private void leastRecent(ArrayList<JSONObject> jsonList) {
        Collections.sort(jsonList, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject a, JSONObject b) {
                String valA = new String();
                String valB = new String();
                try {
                    if(a.has("time_created")) {
                        valA = (String) a.getString("time_created");
                        valB = (String) b.getString("time_created");
                    } else {
                        valA = (String)a.getString("time");
                        valB = (String)b.getString("time");
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                return valA.compareTo(valB);
            }
        });
    }


    private void createSpinner(Spinner spin,String[] strArr) {
        ArrayAdapter aa = new ArrayAdapter(getActivity(),android.R.layout.simple_spinner_item,strArr);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(aa);
    }


    private void getJsonList(ArrayList<JSONObject> jsonList, JSONArray jsonarray) {
        for (int i = 0; i < jsonarray.length(); i++) {
            try {
                jsonList.add(jsonarray.getJSONObject(i));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean include(JSONObject object,String key){
        try {
            JSONArray arr = object.getJSONArray("types");
            for(int i = 0; i<arr.length();i++){
                if(arr.getString(i).equals(key)){
                    return true;
                }
            }
        }catch (JSONException e){
            return false;
        }
        return false;
    }

    private void getYelpReviews() {
        try {
            name = jsonObj.getString("name");
            JSONArray addr = jsonObj.getJSONArray("address_components");
            for(int i= 0 ; i < addr.length(); i++){
                if(include(addr.getJSONObject(i),"country")){
                    country = addr.getJSONObject(i).getString("short_name");
                }else if(include(addr.getJSONObject(i),"administrative_area_level_1")){
                    state = addr.getJSONObject(i).getString("short_name");
                }else if(include(addr.getJSONObject(i),"locality")){
                    city = addr.getJSONObject(i).getString("short_name");
                }
            }
            address1 = jsonObj.getString("formatted_address").split(",")[0];
            getSearchResults();
        }catch (JSONException e){

        }

    }

    private void getSearchResults() {
        String url = "http://cs571hw8-env.us-west-1.elasticbeanstalk.com";
        String params = ("?name="+ name + "&address1=" + address1 + "&city=" + city + "&state=" + state + "&country=" + country).replace(' ','+');
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url + params, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
               try {
                   jsonResponse = response.getJSONArray("reviews");
                   initYelp = jsonResponse;
               } catch(JSONException e) {
                   throw new RuntimeException(e);
               }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO: Handle error
            }
        });
        queue.add(jsonObjectRequest);
    }
}
