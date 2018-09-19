package com.example.kj.searchforfun;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.RuntimeExecutionException;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.ViewHolder> {
    public JSONArray mDataset;

    public RecycleAdapter(JSONArray myDataset) {
        mDataset = myDataset;
    }

    @Override
    public RecycleAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view =  LayoutInflater.from(parent.getContext()).inflate(R.layout.place, parent, false);
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

        public ViewHolder(View v) {
            super(v);
            mView = v;
        }

        public void setView(final JSONObject jsonObj) {
            ImageView imageView = (ImageView) mView.findViewById(R.id.categoryImage);
            TextView placeName = (TextView) mView.findViewById(R.id.placeName);
            TextView placeAddress = (TextView) mView.findViewById(R.id.placeAddress);
            final ImageButton favoriteButton = (ImageButton) mView.findViewById(R.id.favoriteButton);
            final LinearLayout placeNameAndAddress = (LinearLayout) mView.findViewById(R.id.placeNameAndAddress);
            try {
                Picasso.get().load(jsonObj.getString("icon")).into(imageView);
                placeName.setText(jsonObj.getString("name"));
                placeAddress.setText(jsonObj.getString("vicinity"));
                if(MainActivity.mRecycleAdapter.mDataset.toString().contains(jsonObj.getString("place_id"))) {
                    favoriteButton.setImageResource(R.drawable.ic_favorite_red_24dp);
                    favoriteButton.setTag(R.drawable.ic_favorite_red_24dp);
                } else {
                    favoriteButton.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                    favoriteButton.setTag(R.drawable.ic_favorite_border_black_24dp);
                }
                addListenerOnFavoriteButton(favoriteButton, jsonObj);
                addListenerOnPlaceNameAndAddress(placeNameAndAddress, jsonObj);
            }catch(JSONException e) {
                throw new RuntimeExecutionException(e);
            }
        }

        private void addListenerOnFavoriteButton(final ImageButton favoriteButton, final JSONObject jsonObj) {
            favoriteButton.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public void onClick(View view) {
                    if((Integer)favoriteButton.getTag() == R.drawable.ic_favorite_red_24dp) {
                        favoriteButton.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                        favoriteButton.setTag(R.drawable.ic_favorite_border_black_24dp);
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

        private void addListenerOnPlaceNameAndAddress(LinearLayout placeNameAndAddress, final JSONObject jsonObj) {
            placeNameAndAddress.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MainActivity.dialog = new ProgressDialog(view.getContext());
                    MainActivity.dialog.setMessage("Fetching details");
                    MainActivity.dialog.show();
                    final Intent intent = new Intent(MainActivity.activity, DetailsActivity.class);
                    try {
                        final String data = jsonObj.getString("place_id");
                        RequestQueue queue = Volley.newRequestQueue(view.getContext());
                        String url = "http://cs-server.usc.edu:14344/place.php";
                        String params = "?placeid=" + data;
                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url + params, null, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try{
                                    MainActivity.dialog.dismiss();
                                    intent.putExtra(MainActivity.searchKey, response.getJSONObject("result").toString());
                                    MainActivity.activity.startActivity(intent);
                                } catch (JSONException e) {
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
                    } catch (JSONException e) {
                        throw new RuntimeExecutionException(e);
                    }
                }
            });
        }
    }
}
