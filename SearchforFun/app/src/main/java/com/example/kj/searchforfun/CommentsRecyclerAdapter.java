package com.example.kj.searchforfun;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;



public class CommentsRecyclerAdapter extends RecyclerView.Adapter<CommentsRecyclerAdapter.ViewHolder> {
    public JSONArray mDataset;
    private String name;
    private String text;
    private String rating;
    private String icon;
    private String url;
    private String time;



    public CommentsRecyclerAdapter(JSONArray myDataset) {
        mDataset = myDataset;
    }

    @Override
    public CommentsRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view =  LayoutInflater.from(parent.getContext()).inflate(R.layout.review, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        try{
            holder.setView(mDataset.getJSONObject(position));
        }catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getItemCount() {
        return mDataset.length();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private View mView;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setView(final JSONObject jsonObj) {
            ImageView userPhoto = (ImageView) mView.findViewById(R.id.userPhoto);
            TextView userName = (TextView) mView.findViewById(R.id.userName);
            RatingBar ratingStars = (RatingBar) mView.findViewById(R.id.ratingStars);
            TextView timeView = (TextView) mView.findViewById(R.id.time);
            TextView content = (TextView) mView.findViewById(R.id.content);

            if (jsonObj.has("author_name")) {
                loadGoogle(jsonObj);
            } else {
                loadYelp(jsonObj);
            }
            Picasso.get().load(icon).into(userPhoto);
            userName.setText(name);
            ratingStars.setRating(Float.parseFloat(rating));
            timeView.setText(time);
            content.setText(text);
        }

        public void loadYelp(JSONObject json){
            try {
                name = json.getJSONObject("user").getString("name");
                time = json.getString("time_created");
                text = json.getString("text");
                rating = json.getInt("rating")+"";
                icon = json.getJSONObject("user").getString("image_url");
                url = json.getString("url");
            }catch (JSONException e){

            }
        }

        public void loadGoogle(JSONObject json){
            try {
                name = json.getString("author_name");
                long timestamp =Long.parseLong(json.getString("time"))*1000;
                text = json.getString("text");
                rating = json.getInt("rating")+"";
                icon = json.getString("profile_photo_url");
                url = json.getString("author_url");
                SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                Date date = new Date(timestamp);
                time = dt.format(date);
            }catch (JSONException e){

            }
        }
    }
}
