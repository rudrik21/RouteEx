package com.example.routeex;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.routeex.models.DirectionsModels.DirectionsData;
import com.example.routeex.models.DirectionsModels.LegsItem;
import com.example.routeex.models.DirectionsModels.Polyline;
import com.example.routeex.models.DirectionsModels.RoutesItem;
import com.example.routeex.models.DirectionsModels.StepsItem;
import com.example.routeex.models.RestaurantModels.RestaurantsData;
import com.example.routeex.models.RestaurantModels.ResultsItem;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback{

    private Response res = null;
    private static int REQ_CODE = 1;
    GoogleMap mMap;

    //  get user location
    private FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;
    LocationRequest locationRequest;

    private LatLng userLoc = null;
    private List<LatLng> markers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        initMap();

        getUserLocation();

        if (!checkPermission()){
            requestPermission();
        }else{
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        }
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private boolean checkPermission(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            return true;
        }
        return false;
    }

    private void requestPermission(){
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_CODE);
    }

    private void getUserLocation(){
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10);

        setHomeMarker();
    }

    private void setHomeMarker() {
        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location l: locationResult.getLocations()){
                    LatLng userLocation = new LatLng(l.getLatitude(), l.getLongitude());
                    userLoc = userLocation;

                    CameraPosition position = CameraPosition.builder()
                            .target(userLocation)
                            .zoom(15)
                            .bearing(0)
                            .tilt(45)
                            .build();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));
                    mMap.addMarker(new MarkerOptions()
                            .position(userLocation)
                            .icon(bitmapFromVector(R.drawable.ic_loc))
                            .title("Your location!"));
                }
            }
        };
    }

    private BitmapDescriptor bitmapFromVector(int id){
        Drawable drawable = ContextCompat.getDrawable(this, id);
        drawable.setBounds(0,0,drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQ_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setHomeMarker();
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            }
        }
    }

    public void btnClick(View v){
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+String.valueOf(userLoc.latitude)+","+String.valueOf(userLoc.longitude)+"&radius=500&types=restaurant&key=AIzaSyDK2Du7rvxW4d4NQmKg8qAyxaZ0dGgaY5k";
        System.out.println(url);
        if (v instanceof Button){
            Button btn = (Button) v;

            switch (btn.getId()){
                case R.id.btn_restaurant:
                    if (userLoc != null) {

                            RequestQueue queue = Volley.newRequestQueue(this);


                            StringRequest req = new StringRequest(Request.Method.GET, url,
                                new Response.Listener<String>(){

                                    @Override
                                    public void onResponse(String response) {
//                                        System.out.println(response);

                                        RestaurantsData res = new Gson().fromJson(response, RestaurantsData.class);
                                        onResult((res));

                                    }
                                }, new Response.ErrorListener(){

                                @Override
                                public void onErrorResponse(VolleyError error) {
//                                    System.out.println(error.getMessage());
                                }

                            });

                            queue.add(req);
                    }
                    break;
            }
        }
    }

    private void onResult(RestaurantsData res){
        if (res != null) {
            for (ResultsItem r : res.getResults()) {
                double lat = r.getGeometry().getLocation().getLat();
                double lng = r.getGeometry().getLocation().getLng();
                LatLng latLng = new LatLng(lat, lng);

                if (!markers.contains(latLng)){
                    markers.add(latLng);
                }

                if (markers.contains(latLng)){
                    MarkerOptions opt = new MarkerOptions()
                            .position(latLng)
                            .icon(BitmapDescriptorFactory.defaultMarker())
                            .title(r.getName());
                    Marker m = mMap.addMarker(opt);
                }
            }

            mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    showPath(userLoc, marker.getPosition());
                }
            });
        }
    }

    private void showPath(LatLng source, LatLng dest){
        System.out.println("Source: " + source);
        System.out.println("Destination: " + dest);
        String src = String.valueOf(source.latitude)+","+String.valueOf(source.longitude);
        String dst = String.valueOf(dest.latitude)+","+String.valueOf(dest.longitude);
        String url = "https://maps.googleapis.com/maps/api/directions/json?origin="+src+"&destination="+dst+"&mode=driving&key=AIzaSyDK2Du7rvxW4d4NQmKg8qAyxaZ0dGgaY5k";
        System.out.println("Path: " + url);

        RequestQueue queue = Volley.newRequestQueue(this);


        StringRequest req = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>(){

                    @Override
                    public void onResponse(String response) {
//                                        System.out.println(response);

                        DirectionsData res = new Gson().fromJson(response, DirectionsData.class);
                        PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
                        List<LatLng> points = new ArrayList<>();
                        for (RoutesItem route : res.getRoutes()) {
                            for (LegsItem leg : route.getLegs()) {
                                for (StepsItem step : leg.getSteps()) {
                                    points.addAll(PolyUtil.decode(String.valueOf(step.getPolyline().getPoints())));
                                }
                            }
                        }
                        options.addAll(points);
                        options.width(12);
                        options.color(Color.RED);
                        options.geodesic(true);
                        mMap.addPolyline(options);

                    }
                }, new Response.ErrorListener(){

            @Override
            public void onErrorResponse(VolleyError error) {
//                                    System.out.println(error.getMessage());
            }

        });

        queue.add(req);
    }

}
