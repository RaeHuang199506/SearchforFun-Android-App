package com.example.kj.searchforfun;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class SearchResultsActivity extends AppCompatActivity {
    private JSONObject jsonObj;
    private String pageToken;
    private boolean existThirdPage;
    private Toolbar toolbar;
    private Button nextButton;
    private Button previousButton;
    private Intent intent;
    private String message;
    public static RecycleAdapter[] searchAdapters;
    public static int currPage;
    private PlacesTableFragment searchResults;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);
        MainActivity.activity = this;
        init();
        setFirstAdapaters();
        setFragment();
        addListenerOnNextButton();
        addListenerOnPreviousButton();
    }

    private void init() {
        initToolbar();
        nextButton = (Button) findViewById(R.id.next);
        previousButton = (Button) findViewById(R.id.previous);
        intent = getIntent();
        message = intent.getStringExtra(MainActivity.searchKey);
        MainActivity.dialog.dismiss();
        searchAdapters =  new RecycleAdapter[3];
        queue = Volley.newRequestQueue(this);
        currPage = 0;
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Search results");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setFirstAdapaters() {
        try{
            jsonObj = new JSONObject(message);
            if(!jsonObj.toString().contains("next_page_token")) {
                nextButton.setEnabled(false);
            }else {
                pageToken = jsonObj.getString("next_page_token");
                nextButton.setEnabled(true);
            }
            previousButton.setEnabled(false);
            searchAdapters[0] = new RecycleAdapter(jsonObj.getJSONArray("results"));
            searchResults = PlacesTableFragment.newInstance(searchAdapters[0]);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void setFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.searchResults, searchResults);
        fragmentTransaction.commit();
    }

    private void addListenerOnNextButton() {
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (searchAdapters[++currPage] == null) {
                    MainActivity.dialog = new ProgressDialog(view.getContext());
                    MainActivity.dialog.setMessage("Fetching next page");
                    MainActivity.dialog.show();
                    getNextSearchResults();
                } else {
                    if((!existThirdPage) || currPage == 2) {
                        nextButton.setEnabled(false);
                    } else {
                        nextButton.setEnabled(true);
                    }
                    previousButton.setEnabled(true);
                    PlacesTableFragment.searchAdapter = searchAdapters[currPage];
                    PlacesTableFragment.mRecyclerView.setAdapter(PlacesTableFragment.searchAdapter);
                }
            }
        });
    }

    private void getNextSearchResults() {
        String url = "http://cs571hw8-env.us-west-1.elasticbeanstalk.com";
        String params = "?pagetoken=" + pageToken;
        //Toast.makeText(getApplicationContext(), pageToken, Toast.LENGTH_SHORT).show();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url + params, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                jsonObj = response;
                try{
                    if(!jsonObj.toString().contains("next_page_token")) {
                        nextButton.setEnabled(false);
                    }else {
                        existThirdPage = true;
                        pageToken = jsonObj.getString("next_page_token");
                        nextButton.setEnabled(true);
                    }
                    previousButton.setEnabled(true);
                    searchAdapters[currPage] = new RecycleAdapter(jsonObj.getJSONArray("results"));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                MainActivity.dialog.dismiss();
                PlacesTableFragment.searchAdapter = searchAdapters[currPage];
                PlacesTableFragment.mRecyclerView.setAdapter(PlacesTableFragment.searchAdapter);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO: Handle error
            }
        });
        queue.add(jsonObjectRequest);
    }

    private void addListenerOnPreviousButton() {
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(--currPage == 0) {
                    previousButton.setEnabled(false);
                } else {
                    previousButton.setEnabled(true);
                }
                nextButton.setEnabled(true);
                PlacesTableFragment.searchAdapter = searchAdapters[currPage];
                PlacesTableFragment.mRecyclerView.setAdapter(PlacesTableFragment.searchAdapter);
            }
        });
    }
}
