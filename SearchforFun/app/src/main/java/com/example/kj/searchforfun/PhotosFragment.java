package com.example.kj.searchforfun;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.ImageView;
import android.widget.Toast;


import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

public class PhotosFragment extends Fragment {
    private View frag;
    private JSONObject jsonObj;
    private String placeId;
    private ScrollView scrollPhotos;
    private LinearLayout imageLayout;
    private int screenWidth;
    //private ImageView mImageView;
    private GeoDataClient mGeoDataClient;

    public static PhotosFragment newInstance(JSONObject jsonObject) {
        PhotosFragment photosFragment = new PhotosFragment();
        photosFragment.jsonObj = jsonObject;
        return photosFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        frag = inflater.inflate(R.layout.photos_tab, container, false);
        init();
        getPhotos();
        return frag;
    }

    private void init() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        scrollPhotos = (ScrollView) frag.findViewById(R.id.scrollPhotos);
        imageLayout = (LinearLayout) frag.findViewById(R.id.imageLayout);
        //mImageView = (ImageView) frag.findViewById(R.id.image);
        mGeoDataClient = Places.getGeoDataClient(getActivity(), null);
    }

    // Request photos and metadata for the specified place.
    private void getPhotos() {
        try {
            placeId = jsonObj.getString("place_id");
        } catch(JSONException e) {
            throw new RuntimeException(e);
        }
        final Task<PlacePhotoMetadataResponse> photoMetadataResponse = mGeoDataClient.getPlacePhotos(placeId);
        photoMetadataResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoMetadataResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlacePhotoMetadataResponse> task) {
                PlacePhotoMetadataResponse photos = task.getResult();
                PlacePhotoMetadataBuffer photoMetadataBuffer = photos.getPhotoMetadata();
                int i = 0;
                for(PlacePhotoMetadata photoMetadata: photoMetadataBuffer){
                    //PlacePhotoMetadata photoMetadata = photoMetadataBuffer.get(i);
                    CharSequence attribution = photoMetadata.getAttributions();
                    Task<PlacePhotoResponse> photoResponse = mGeoDataClient.getPhoto(photoMetadata);
                    photoResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoResponse>() {
                        @Override
                        public void onComplete(@NonNull Task<PlacePhotoResponse> task) {
                            PlacePhotoResponse photo = task.getResult();
                            Bitmap bitmap = photo.getBitmap();
                            ImageView imageView = new ImageView(getActivity());
                            imageView.setImageBitmap(bitmap);
                            imageView.setPadding(0,50,0,0);
                            imageLayout.addView(imageView);
                            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) imageView.getLayoutParams();
                            params.width = screenWidth;
                            params.height = screenWidth * bitmap.getHeight() / bitmap.getWidth();
                            imageView.setLayoutParams(params);
                        }
                    });
                }
            }
        });
    }
}

