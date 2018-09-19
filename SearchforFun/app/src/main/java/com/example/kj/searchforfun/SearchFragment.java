package com.example.kj.searchforfun;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.data.DataBufferUtils;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.AutocompletePredictionBufferResponse;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import android.Manifest;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.TextViewCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView ;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONObject;
import org.json.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.app.Activity.RESULT_OK;


public class SearchFragment extends Fragment {
    private GeoDataClient mGeoDataClient;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;

    private String[] category = {
            "Default", "Airport", "Amusement Aark", "Aquarium", "Art Gallery",
            "Bakery", "Bar", "Beauty Salon", "Bowling Alley", "Bus Station",
            "Cafe", "Campground", "Car Rental", "Casino", "Lodging",
            "Movie Theater", "Museum", "Night Club", "Park", "Parking",
            "Restaurant", "Shopping Mall", "Stadium", "Subway Station", "Taxi Stand",
            "Train Station", "Transit Station", "Travel Agency", "Zoo"
    };

    private RadioGroup radioGroup;
    private RadioButton radioLocation;
    private Button searchButton;
    private Button clearButton;
    private View frag;
    private EditText keyword;
    private EditText distance;
    private Spinner spin;
    private AutoCompleteTextView inputLocation;
    private TextView validationMessage1;
    private TextView validationMessage2;
    private Location currLocation;
    private RadioButton fromLocation;
    private RadioButton specLocation;
    private double currLat;
    private double currLng;
    private double inputLat;
    private double inputLng;
    private double searchLat;
    private double searchLng;
    private RequestQueue queue;
    private JSONObject jsonObj;
    private ArrayAdapter<CharSequence> arrayAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        frag = inflater.inflate(R.layout.search_tab, container, false);
        initValue();
        createSpinner();
        getPermissionandCurrLocation();
        addListenerOnSearchButton();
        addListenerOnClearButton();
        inputLocation.addTextChangedListener(new TextChangedListener<EditText>(inputLocation) {
            @Override
            public void onTextChanged(EditText target, final Editable s) {
                //Toast.makeText(getActivity(), target.getText().toString(), Toast.LENGTH_SHORT).show();
                Task<AutocompletePredictionBufferResponse> task = mGeoDataClient.getAutocompletePredictions(s.toString(), null, null);
                task.addOnCompleteListener(new OnCompleteListener<AutocompletePredictionBufferResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<AutocompletePredictionBufferResponse> task) {
                        if(task.isSuccessful()) {
                            AutocompletePredictionBufferResponse predictions = task.getResult();
                            arrayAdapter.clear();
                            for(AutocompletePrediction prediction: predictions) {
                                arrayAdapter.add(prediction.getFullText(null));
                            }
                            arrayAdapter.notifyDataSetChanged();
                            arrayAdapter.getFilter().filter(s);
                        }
                    }
                });
            }
        });
        return frag;
    }

    private void initValue() {
        mLocationRequest = new LocationRequest();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
        mGeoDataClient = Places.getGeoDataClient(getActivity(), null);
        arrayAdapter = new ArrayAdapter<CharSequence>(getContext(),android.R.layout.simple_list_item_1,new ArrayList<CharSequence>());
        queue = Volley.newRequestQueue(getActivity());
        keyword = (EditText) frag.findViewById(R.id.keyword);
        distance = (EditText) frag.findViewById(R.id.distance);
        spin =  (Spinner) frag.findViewById(R.id.spinner);
        radioGroup = (RadioGroup) frag.findViewById(R.id.radioGroup);
        inputLocation = (AutoCompleteTextView) frag.findViewById(R.id.inputLocation);
        searchButton = (Button) frag.findViewById(R.id.searchButton);
        fromLocation = (RadioButton) frag.findViewById(R.id.currLocation);
        specLocation = (RadioButton) frag.findViewById(R.id.specLocation);
        clearButton = (Button) frag.findViewById(R.id.clearButton);
        validationMessage1 = (TextView) frag.findViewById(R.id.validationMessage1);
        validationMessage2 = (TextView) frag.findViewById(R.id.validationMessage2);
        inputLocation.setAdapter(arrayAdapter);
        radioLocation = (RadioButton) frag.findViewById(radioGroup.getCheckedRadioButtonId());
        //addListenserOnRadioButton();
    }

    /*private void addListenserOnRadioButton() {
        radioLocation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(radioLocation.getText().toString().equals("Other. Specify location")) {
                    validationMessage2.setEnabled(true);
                } else {
                    validationMessage2.setEnabled(false);
                }
            }
        });
    }*/

    private void getPermissionandCurrLocation() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            if(GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getActivity())!=ConnectionResult.SUCCESS){
                Toast.makeText(getActivity(), "Google Play Service Not Available", Toast.LENGTH_SHORT).show();
            }
            mLocationRequest.setInterval(2000); // two minute interval
            mLocationRequest.setFastestInterval(1000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION);
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,mLocationCallback, Looper.myLooper());
        }
    }

    LocationCallback mLocationCallback = new LocationCallback(){
        /*@Override
        public void onLocationAvailability(LocationAvailability locationAvailability) {
            super.onLocationAvailability(locationAvailability);
        }*/
        @Override
        public void onLocationResult(LocationResult locationResult) {
            currLocation = locationResult.getLastLocation();
            currLat = currLocation.getLatitude();
            currLng = currLocation.getLongitude();
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if(requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getPermissionandCurrLocation();
            }
        }
    }

    private void createSpinner() {
        ArrayAdapter aa = new ArrayAdapter(getActivity(),android.R.layout.simple_spinner_item,category);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(aa);
    }

    private  void addListenerOnSearchButton() {
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (radioGroup.getCheckedRadioButtonId() == R.id.specLocation && !isValidInput(inputLocation)) {
                    Toast.makeText(getActivity(), "Please fix all fields with errors", Toast.LENGTH_SHORT).show();
                    validationMessage2.setVisibility(v.VISIBLE);
                } else {
                    validationMessage2.setVisibility(v.GONE);
                }

                if (!isValidInput(keyword)) {
                    Toast.makeText(getActivity(), "Please fix all fields with errors", Toast.LENGTH_SHORT).show();
                    validationMessage1.setVisibility(v.VISIBLE);
                } else {
                    validationMessage1.setVisibility(v.GONE);
                    if ((radioGroup.getCheckedRadioButtonId() == R.id.specLocation && isValidInput(inputLocation))|| radioGroup.getCheckedRadioButtonId() == R.id.currLocation){
                        validationMessage2.setVisibility(v.GONE);
                        if (radioLocation.getText().toString().equals("Other. Specify location")) {
                            Address loc = getLocationFromAddress();
                            searchLat = loc.getLatitude();
                            searchLng = loc.getLongitude();
                            MainActivity.dialog = new ProgressDialog(getActivity());
                            MainActivity.dialog.setMessage("Fetching results");
                            MainActivity.dialog.show();
                            getSearchResults();
                        } else {
                            if (currLocation != null) {
                                MainActivity.dialog = new ProgressDialog(getActivity());
                                MainActivity.dialog.setMessage("Fetching results");
                                MainActivity.dialog.show();
                                searchLat = currLat;
                                searchLng = currLng;
                                getSearchResults();
                            } else {
                                Toast.makeText(getActivity(), "Didn't get location successfully.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            }
        });

    }

    private void addListenerOnClearButton() {
        clearButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                keyword.setText("");
                spin.setSelection(0);
                distance.setText("");
                RadioButton fromLocation = (RadioButton) frag.findViewById(R.id.currLocation);
                fromLocation.setChecked(true);
                validationMessage1.setVisibility(v.GONE);
                validationMessage1.setVisibility(v.GONE);
                inputLocation.setText("");
            }
        });

        ;

    }

    private Address getLocationFromAddress() {
        Geocoder geocoder = new Geocoder(getContext());
        List<Address> address;
        try{
            address = geocoder.getFromLocationName(inputLocation.getText().toString(),1);
            if(address == null) {
                Toast.makeText(getActivity(), "No results", Toast.LENGTH_SHORT).show();
                return null;
            }
            return address.get(0);
        }catch(IOException e) {
            Toast.makeText(getActivity(), "IO Exception", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private boolean isValidInput(EditText etText) {
        if(etText.getText().toString().trim().length() > 0) {
            return true;
        } else {
            return false;
        }
    }

    private void getSearchResults() {
        if(distance.getText().length() == 0) {
            distance.setText("10");
        }
        String url = "http://cs571hw8-env.us-west-1.elasticbeanstalk.com";
        String params = "?location=" + searchLat + "," + searchLng + "&radius=" + Double.parseDouble(distance.getText().toString()) * 1600 + "&type=" + spin.getSelectedItem().toString().replace(" ", "_").toLowerCase() + "&keyword=" + keyword.getText().toString();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url + params, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                jsonObj = response;
                sendSearchResults();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO: Handle error
            }
        });
        queue.add(jsonObjectRequest);
    }

    public void sendSearchResults() {
        Intent intent = new Intent(getActivity(), SearchResultsActivity.class);
        String data = jsonObj.toString();
        intent.putExtra(MainActivity.searchKey, data);
        startActivity(intent);
    }


}
