package com.example.kj.searchforfun;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class DetailsActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private Intent intent;
    private static JSONObject jsonObj;
    private String message;
    private String title;
    private ImageButton favoriteButton;
    private ImageButton shareButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_results);
        MainActivity.activity = this;
        initView();
        addListenerOnFavoriteButton();
        addListenerOnShareButton();
    }

    private void initView() {
        initToolbar();
        viewPager = (ViewPager) findViewById(R.id.pager_details);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs_details);
        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons();

        intent = getIntent();
        message = intent.getStringExtra(MainActivity.searchKey);
        favoriteButton = (ImageButton) findViewById(R.id.favoriteButton);
        shareButton = (ImageButton) findViewById(R.id.shareButton);
        try {
            jsonObj = new JSONObject(message);
            title = jsonObj.getString("name");
        }catch(JSONException e) {
            throw new RuntimeException(e);
        }
        setTitle(title);
        initFavorte();
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar_details);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchResultsActivity.searchAdapters[SearchResultsActivity.currPage].notifyDataSetChanged();
                finish();
            }
        });
    }

    private void setupTabIcons() {
        TextView tabOne = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_icon, null);
        tabOne.setText("INFO");
        tabOne.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_info, 0, 0, 0);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        tabOne.setLayoutParams(params);
        tabLayout.getTabAt(0).setCustomView(tabOne);

        TextView tabTwo = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_icon, null);
        tabTwo.setText("PHOTOS");
        tabTwo.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_photo, 0, 0, 0);
        tabTwo.setLayoutParams(params);
        tabLayout.getTabAt(1).setCustomView(tabTwo);

        TextView tabThree = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_icon, null);
        tabThree.setText("MAP");
        tabThree.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_map, 0, 0, 0);
        tabThree.setLayoutParams(params);
        tabLayout.getTabAt(2).setCustomView(tabThree);

        TextView tabFour = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_icon, null);
        tabFour.setText("REVIEWS");
        tabFour.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_review, 0, 0, 0);
        tabFour.setLayoutParams(params);
        tabLayout.getTabAt(3).setCustomView(tabFour);
    }

    private void setupViewPager(ViewPager viewPager){
        DetailsPagerAdapter adapter = new DetailsPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(InfoFragment.newInstance(DetailsActivity.getDetailsObject()), "INFO");
        adapter.addFrag(PhotosFragment.newInstance(DetailsActivity.getDetailsObject()), "PHOTOS");
        adapter.addFrag(MapFragment.newInstance(DetailsActivity.getDetailsObject()), "MAP");
        adapter.addFrag(CommentsFragment.newInstance(DetailsActivity.getDetailsObject()), "REVIEWS");
        viewPager.setAdapter(adapter);
    }

    public static JSONObject getDetailsObject() {
        return jsonObj;
    }

    private void initFavorte() {
        for(int i = 0; i < MainActivity.mRecycleAdapter.mDataset.length(); i++) {
            try {
                if(MainActivity.mRecycleAdapter.mDataset.get(i).toString().contains(jsonObj.getString("place_id"))) {
                    favoriteButton.setImageResource(R.drawable.ic_favorite_red_24dp);
                    favoriteButton.setTag(R.drawable.ic_favorite_red_24dp);
                    break;
                } else {
                    favoriteButton.setImageResource(R.drawable.ic_favorite_white_24dp);
                    favoriteButton.setTag(R.drawable.ic_favorite_white_24dp);
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void addListenerOnFavoriteButton() {
        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                if((Integer)favoriteButton.getTag() == R.drawable.ic_favorite_red_24dp) {
                    favoriteButton.setImageResource(R.drawable.ic_favorite_white_24dp);
                    favoriteButton.setTag(R.drawable.ic_favorite_white_24dp);
                    for(int i = 0; i < MainActivity.mRecycleAdapter.mDataset.length(); i++) {
                        try {
                            if(MainActivity.mRecycleAdapter.mDataset.get(i).toString().contains(jsonObj.getString("place_id"))) {
                                MainActivity.mRecycleAdapter.mDataset.remove(i);
                                Toast.makeText(MainActivity.activity, jsonObj.getString("name") + " was removed from favorites", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                } else {
                    favoriteButton.setImageResource(R.drawable.ic_favorite_red_24dp);
                    favoriteButton.setTag(R.drawable.ic_favorite_red_24dp);
                    MainActivity.mRecycleAdapter.mDataset.put(jsonObj);
                    try{
                        Toast.makeText(MainActivity.activity, jsonObj.getString("name") + " was added to favorites", Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
                MainActivity.mRecycleAdapter.notifyDataSetChanged();
            }
        });
    }

    private void addListenerOnShareButton() {
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String url="https://twitter.com/intent/tweet?text=Check out "+jsonObj.getString("name")+" located at "+jsonObj.getString("formatted_address")+". Website: "+jsonObj.getString("website")+" #TravelAndEntertainmentSearch";
                    url=url.replace(' ','+');
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browserIntent);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
