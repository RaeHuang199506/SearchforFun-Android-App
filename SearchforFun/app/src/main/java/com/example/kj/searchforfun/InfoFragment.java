package com.example.kj.searchforfun;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.RatingBar;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;


public class InfoFragment extends Fragment {
    private View frag;
    private JSONObject jsonObj;
    private TextView address;
    private TextView phoneNumber;
    private TextView priceLevel;
    private RatingBar rating;
    private TextView googlePage;
    private TextView website;

    public static InfoFragment newInstance(JSONObject jsonObject) {
        InfoFragment infoFragment = new InfoFragment();
        infoFragment.jsonObj = jsonObject;
        return infoFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        frag = inflater.inflate(R.layout.info_tab, container, false);
        init();
        return frag;
    }

    private void init() {
        address = (TextView) frag.findViewById(R.id.address);
        LinearLayout row_address = (LinearLayout) frag.findViewById(R.id.row_address);
        phoneNumber = (TextView) frag.findViewById(R.id.phoneNumber);
        LinearLayout row_phoneNumber = (LinearLayout) frag.findViewById(R.id.row_phoneNumber);
        priceLevel = (TextView) frag.findViewById(R.id.priceLevel);
        LinearLayout row_priceLevel = (LinearLayout) frag.findViewById(R.id.row_priceLevel);
        rating = (RatingBar) frag.findViewById(R.id.rating);
        LinearLayout row_rating = (LinearLayout) frag.findViewById(R.id.row_rating);
        googlePage = (TextView) frag.findViewById(R.id.googlePage);
        LinearLayout row_goolePage = (LinearLayout) frag.findViewById(R.id.row_goolePage);
        website = (TextView) frag.findViewById(R.id.website);
        LinearLayout row_website = (LinearLayout) frag.findViewById(R.id.row_website);
        initValue("formatted_address", row_address, address);
        initValue("formatted_phone_number", row_phoneNumber, phoneNumber);
        Linkify.addLinks(phoneNumber, Linkify.PHONE_NUMBERS);
        if(jsonObj.has("price_level")) {
            String dollars = "";
            try {
                for(int i = 0; i < jsonObj.getString("price_level").length(); i++) {
                    dollars += "$";
                }
                priceLevel.setText(dollars);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        } else {
            row_priceLevel.setVisibility(View.GONE);
        }

        try {
            if(jsonObj.has("rating")) {
                rating.setRating(Float.parseFloat(jsonObj.getString("rating")));
                //Toast.makeText(getActivity(), String.valueOf(rating.getRating()), Toast.LENGTH_SHORT).show();
            } else {
                row_rating.setVisibility(View.GONE);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        initValue("url", row_goolePage, googlePage);
        Linkify.addLinks(googlePage, Linkify.WEB_URLS);
        initValue("website", row_website, website);
        Linkify.addLinks(website, Linkify.WEB_URLS);
    }

    private void initValue(String str, LinearLayout row, TextView textView) {
        try {
            if(jsonObj.has(str)) {
                textView.setText(jsonObj.getString(str));
            } else {
                row.setVisibility(View.GONE);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
