package com.example.kj.searchforfun;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Places;

import org.json.JSONArray;


public class MainActivity extends AppCompatActivity{
    public static ProgressDialog dialog;
    public static RecycleAdapter mRecycleAdapter;
    public static String  searchKey = "searchKey";
    public static Activity activity;
    private Toolbar toolbar;
    private ViewPager viewPager;
    private TabLayout tabLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = this;
        mRecycleAdapter = new RecycleAdapter(new JSONArray());
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        viewPager = (ViewPager) findViewById(R.id.pager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons();
    }

    private void setupTabIcons() {
        TextView tabOne = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_icon, null);
        tabOne.setText("SEARCH");
        tabOne.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search_white_24dp, 0, 0, 0);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        tabOne.setLayoutParams(params);
        tabLayout.getTabAt(0).setCustomView(tabOne);

        TextView tabTwo = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_icon, null);
        tabTwo.setText("FAVORITE");
        tabTwo.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_white_24dp, 0, 0, 0);
        tabTwo.setLayoutParams(params);
        tabLayout.getTabAt(1).setCustomView(tabTwo);
    }

    private void setupViewPager(ViewPager viewPager){
        PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new SearchFragment(), "SEARCH");
        adapter.addFrag(PlacesTableFragment.newInstance(), "FAVORITE");
        viewPager.setAdapter(adapter);
    }
}
