package com.example.kj.searchforfun;

import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBufferResponse;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MapFragment extends Fragment implements OnMapReadyCallback {
    private View frag;
    private GeoDataClient mGeoDataClient;
    private ArrayAdapter<CharSequence> arrayAdapter;
    private GoogleMap googleMap;
    private AutoCompleteTextView fromPosition;
    private Spinner travelMode;
    private JSONObject jsonObj;
    private Double lat;
    private Double lng;
    private String name;
    private String[] travelModes = {"Driving", "Bicycling", "Transit", "Walking"};
    private PolylineOptions polylineOptions;
    private ArrayList<Polyline> route;
    private Marker mark;
    private RequestQueue requestQueue;


    public static MapFragment newInstance(JSONObject jsonObject) {
        MapFragment mapFragment = new MapFragment();
        mapFragment.jsonObj = jsonObject;
        return mapFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        frag = inflater.inflate(R.layout.map_tab, container, false);
        init();
        createSpinner();
        MapView mapView = (MapView) frag.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);
        return frag;
    }

    private void init() {
        fromPosition = (AutoCompleteTextView) frag.findViewById(R.id.fromPosition);
        travelMode = (Spinner) frag.findViewById(R.id.travelMode);
        mGeoDataClient = Places.getGeoDataClient(getActivity(), null);
        requestQueue = Volley.newRequestQueue(getActivity());
        polylineOptions = new PolylineOptions();
        route = new ArrayList<>();
        arrayAdapter = new ArrayAdapter<CharSequence>(getContext(),android.R.layout.simple_list_item_1,new ArrayList<CharSequence>());
        fromPosition.setAdapter(arrayAdapter);
        try {
            lat = jsonObj.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
            lng = jsonObj.getJSONObject("geometry").getJSONObject("location").getDouble("lng");
            name = jsonObj.getString("name");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        fromPosition.addTextChangedListener(new TextChangedListener<EditText>(fromPosition) {
            @Override
            public void onTextChanged(EditText target, final Editable s) {
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

        travelMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String from = fromPosition.getText().toString();
                if(from.length() == 0) {
                    return;
                }
                getRoute(from);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        fromPosition.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String from = arrayAdapter.getItem(position).toString();
                if(from.length() == 0) {
                    return;
                }
                getRoute(from);
            }
        });
    }

    private void createSpinner() {
        ArrayAdapter aa = new ArrayAdapter(getActivity(),android.R.layout.simple_spinner_item,travelModes);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        travelMode.setAdapter(aa);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng position = new LatLng(lat, lng);
        this.googleMap=googleMap;
        googleMap.addMarker(new MarkerOptions().position(position).title(name)).showInfoWindow();
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 14.0f));
    }

    public void getRoute(String from){
        clearPolylines();
        if(mark!=null){mark.remove();}
        String reqStr = "https://maps.googleapis.com/maps/api/directions/json?origin="+from.replace(' ','+')+"&destination="+lat+","+lng+"&mode="+travelMode.getSelectedItem().toString().toLowerCase()+"&key="+getString(R.string.google_maps_key);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, reqStr, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray routes = response.getJSONArray("routes");
                    JSONObject leg = routes.getJSONObject(0).getJSONArray("legs").getJSONObject(0);
                    polylineOptions = new PolylineOptions();
                    LatLngBounds.Builder builder = LatLngBounds.builder();

                    JSONArray steps = leg.getJSONArray("steps");
                    for(int z=0; z<steps.length();z++) {
                        String points = steps.getJSONObject(z).getJSONObject("polyline").getString("points");
                        List<LatLng> partial = decodePoly(points);
                        polylineOptions.addAll(partial);
                        for(LatLng o : partial){builder.include(o);}
                    }

                    polylineOptions.width(15);
                    polylineOptions.color(Color.BLUE);
                    polylineOptions.geodesic(true);

                    route.add(googleMap.addPolyline(polylineOptions));
                    mark = googleMap.addMarker(new MarkerOptions().position(route.get(0).getPoints().get(0)));

                    LatLngBounds bounds =builder.build();
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds,20));
                }catch (JSONException e){

                }

            }

        },null);
        requestQueue.add(jsonObjectRequest);
    }

    private void clearPolylines(){
        for(Polyline i : route){
            i.remove();
        }
        route.clear();
    }

    private static List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;
            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng(((double) lat / 1E5),
                    ((double) lng / 1E5));
            poly.add(p);
        }
        return poly;
    }
}
