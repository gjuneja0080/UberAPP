package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.Rating;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener,RoutingListener{

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient, googleApiClient;
   GeoDataClient geoDataClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    Location newLocation;
    private Button mLogout, mRequest, confirmDriver, inCar, conclude;
    private EditText mSearchText;
    private LatLng pickupLocation, finalDestination;
    List<LatLng> result = new ArrayList<LatLng>();
    private static final String TAG = "MapsActivity";
    private static final float DEFAULT_ZOOM = 16f;
    private static boolean execOnce = true;
    private String customerID = "";
    private LatLng destinationLatLng;
    private Map<Marker, Integer> cars;
    Address address;
    private TextView disTime, nameCarRating, fareInfo, confirmation, showRating;
    private List<LatLng> polyLineList;
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.primary_dark_material_light};
    private Marker currentCarPos;
    private int numberOfCars = 4;
    private int j;
    private boolean execGeolocate;
    private  List<DriverCred> credentials = new ArrayList<>();
    private RelativeLayout rellayout, arrivalinfo, search_bar, giveDisTime;
    private int operation = 0, flag;
    final  DriverCred d1 =  new DriverCred("Hector", "Toyota Prius", 1,2);
    final  DriverCred d2 =  new DriverCred("Omar", "Audi A4", 1,4);
    final  DriverCred d3 =  new DriverCred("Doug", "Ford Focus", 1,3);
    final  DriverCred d4 =  new DriverCred("Leslie", "Volkswagen Polo", 1,3);
    final  DriverCred d5 =  new DriverCred("Josh", "Lamborghini", 1,5);
    private double fare;
    private double dist,dur;
    private RatingBar mRatingBar;
    private ImageView driverPhoto;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        polyLineList = new ArrayList<>();
        mSearchText = (EditText) findViewById(R.id.input_search);
        cars = new HashMap<Marker, Integer>();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        polylines = new ArrayList<>();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        destinationLatLng = new LatLng(0.0,0.0);

        confirmation = (TextView) findViewById(R.id.confirmation);
        fareInfo = (TextView) findViewById(R.id.fareInfo);
        disTime = (TextView) findViewById(R.id.disTime);
        nameCarRating = (TextView) findViewById(R.id.nameCarRating);
        mLogout = (Button) findViewById(R.id.logout);
        confirmDriver = (Button) findViewById(R.id.callDriver);
        inCar = (Button) findViewById(R.id.inTheCar);
        conclude = (Button) findViewById(R.id.done);
        rellayout = (RelativeLayout) findViewById(R.id.driverInfo);
        arrivalinfo = (RelativeLayout) findViewById(R.id.Arrival);
        search_bar = (RelativeLayout) findViewById(R.id.search_bar);
        giveDisTime = (RelativeLayout) findViewById(R.id.giveDisTime);
        showRating = (TextView) findViewById(R.id.userRating);
        mRatingBar = (RatingBar) findViewById(R.id.ratingBar);
        driverPhoto = (ImageView) findViewById(R.id.driverPhoto);

        //The button to log out of the application
        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MapsActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });



        // The button to confirm the driver after location has been chosen
        confirmDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                operation = 1;
                flag = 1;
                erasePolylines();
                Location temp = new Location("");
                temp.setLongitude(currentCarPos.getPosition().longitude);
                temp.setLatitude(currentCarPos.getPosition().latitude);
                newLocation = temp;
                getRouteToMLocation(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                confirmation.setVisibility(View.INVISIBLE);
                confirmDriver.setVisibility(View.INVISIBLE);
            }
        });

        // The button to indicate when the customer is in the car
        inCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flag = 2;
                newLocation = mLastLocation;
                getRouteToMLocation(finalDestination);
                rellayout.setVisibility(View.INVISIBLE);
                giveDisTime.setVisibility(View.INVISIBLE);
            }
        });

        //The Button to redirect to the map page of the application, after rating has been given and the fare has been displayed
        conclude.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                arrivalinfo.setVisibility(View.INVISIBLE);
                search_bar.setVisibility(View.VISIBLE);
                mLogout.setVisibility(View.VISIBLE);

            }
        });

        //The button to select a rating
        mRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                mRatingBar.getNumStars();

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        //Adding the drivers to the credentials arraylist which is of the type class DriverCred
        credentials.add(d1);
        credentials.add(d2);
        credentials.add(d3);
        credentials.add(d4);
        credentials.add(d5);

        //Marker for selected car
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker driverMarker) {
                //set a boolean condition that states if a destination is set before the marker is selected, this if statement is to ensure, that a car cannot be chosen if a destination isn't searched for first
                if(execGeolocate == true){
                    //Makes the layout visible where the customer can look at driver's credentials, confirm a ride, and then confirm when they're in the car
                    rellayout.setVisibility(View.VISIBLE);
                    currentCarPos = driverMarker;

                    DriverCred driver = credentials.get(cars.get(driverMarker));
                    //The following conditional statements are used to assign each driver with an image, for the customer's perusal
                    if(driver == d1){
                        driverPhoto.setImageDrawable(MapsActivity.this.getResources().getDrawable(R.drawable.chick_hicks));
                    }else if (driver == d2){
                        driverPhoto.setImageDrawable(MapsActivity.this.getResources().getDrawable(R.drawable.mater));
                    }else if(driver == d3){
                        driverPhoto.setImageDrawable(MapsActivity.this.getResources().getDrawable(R.drawable.jackson_storm));
                    }else if(driver == d4){
                        driverPhoto.setImageDrawable(MapsActivity.this.getResources().getDrawable(R.drawable.mcqueen));
                    }
                    else if(driver == d5){
                        driverPhoto.setImageDrawable(MapsActivity.this.getResources().getDrawable(R.drawable.holly_shiftwell));
                    }
                    //Display the selected driver's name, their car name, and their rating
                    nameCarRating.setText("Name: " + driver.driverName + "\n Car: " + driver.carName + "\n Rating: " + driver.rating);
                    //makes the selected driver's photo visible
                    driverPhoto.setVisibility(View.VISIBLE);
                }
                return true;
            }
        });
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
        initialise();
    }

    @Override
    public void onLocationChanged(Location location) {                                                          //Function called every second, get an updated location every second, so we get latitude and longitude
        mLastLocation = location;

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        if(execOnce) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(18));                                                     //Interaction with these indexes; the smaller they are, the closer they are to the ground; this value goes from 1 to 21
            randomiseCars();
            execOnce = false;
        }

    }


    //This method is used to call the geolocate method which enables searching for a location
    private void initialise(){

        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || event.getAction() == KeyEvent.ACTION_DOWN || event.getAction() == KeyEvent.KEYCODE_ENTER){

                    //execute our method for searching
                    getLocation();
                    operation = 0;
                    getRouteToMLocation(pickupLocation);
                    rellayout.setVisibility(View.VISIBLE);

            }
                return false;
            }
        });

    }

    //Method to calculate the fare of the journey, which includes a base fare, which is added into the product of cost per kilometre and the total distance of the journey, which is  further added into the product of cost per minute and the total duration of the journey
    private void getFare(double distance, double duration){
        double basefare = 0.002;
        double costperkm = 0.005;
        double costpermin = 0.007;
        fare = basefare + (distance * costperkm) + (costpermin * duration);
    }

    //Method to find a location in the form of coordinates,
    private void getLocation(){

        Log.d(TAG, "geoLocate: geolocating");

        //Local variable to store the string the user inputs in the search bar
        String searchString = mSearchText.getText().toString();
        //Needed to geographically locate the coordinates of the destination
        Geocoder geocoder = new Geocoder(MapsActivity.this);
        //List to add the location searched by the user
        List<Address> list = new ArrayList<>();

        try{
            //Adds the location in the list
            list = geocoder.getFromLocationName(searchString, 1);
        }catch(IOException e){
        Log.e(TAG, "geoLocate: IOException: " + e.getMessage() );
        }

        if(list.size() > 0){
            //Stores the location in an address type variable
            address = list.get(0);

            Log.d(TAG, "geoLocate: found a location " + address.toString());

            //moves the camera to the location searched by the user
            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM, address.getAddressLine(0));
            double lat = address.getLatitude();
            double lng = address.getLongitude();

            //pickupLocation takes the location of the customer from the location variable mLastLocation
            pickupLocation = (new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
            newLocation = new Location("");
            newLocation.setLatitude(lat);
            newLocation.setLongitude(lng);
            finalDestination = (new LatLng(address.getLatitude(),address.getLongitude()));
            //execGeolocate is set to true so that the customer can choose a driver to complete their journey
            execGeolocate = true;

        }
    }

    //method to move the camera
    private void moveCamera(LatLng latlng, float zoom, String title){
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latlng.latitude + ",lng: " + latlng.longitude);
        //Move the camera to the value of the latlng variable, and then zooms in on that location
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoom));

        if(!title.equals("My Location")) {
            //Puts a red marker sign on the new location(Adds the marker)
            MarkerOptions options = new MarkerOptions().position(latlng).title(title);

            mMap.addMarker(options);
        }
        //hideKeyboard();
    }

    //Method to display multiple cars on the map randomly
    private void randomiseCars(){

        Random random = new Random();

        double coordinates;

        //Loop iteration for placing cars on the map randomly near the customer's location
        for(j = 0; j <= numberOfCars; j++){
            //This coordinates variable is assigned random small value
            coordinates = (double)(random.nextInt(12)  + 1 ) / 6000;
            //This carposition marker variable stores the location of the car by adding the coordinates variable into the latitude and longitude so as to place the car near the latitude and longitude of the customer's location, and then designates the car marker a car icon
            Marker carPosition = mMap.addMarker((new MarkerOptions().position(new LatLng(mLastLocation.getLatitude() + coordinates, mLastLocation.getLongitude() + coordinates)).icon(BitmapDescriptorFactory.fromResource(R.mipmap.car))));
            //The random car locations are then stored in a hashmap of cars, to keep a track of these cars
            cars.put(carPosition, j);
            //Place the car markers on the random locations
            carPosition.getId();

        }
    }

    // animates car on journey(moving car) & gives a list of latlngs for the route
    private void CarAnimation(GoogleMap myMap, final Marker marker, final List<LatLng> directionPoint, final boolean hideMarker) {

        myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(directionPoint.get(0), 15));
        //Defines the handler for the user interface thread
        final Handler handler = new Handler();


        handler.post(new Runnable() {
            int i = 0;
            @Override
            public void run() {
                //Condition to check as long as i is less than the value stored in directionpoint variable(which stores the coordinates of the journey)
                if (i < directionPoint.size()){
                    //Moves the marker(the selected car) to the location of the next set of coordinates stored in directionpoint variable
                    marker.setPosition(directionPoint.get(i));
                    //This is used to show that the car is moving on the polyline, and not jumping to the destination directly
                    handler.postDelayed(this, 20);
                    i++;
                    //The else statement runs when the customer has reached the destination
                }else{
                    if(flag == 1) {
                        inCar.setVisibility(View.VISIBLE);
                        nameCarRating.setText("Your driver has arrived at your location!");
                        driverPhoto.setVisibility(View.INVISIBLE);
                    }
                    if(flag == 2){
                    customerToDest();
                    }
                }
            }
        });
    }

    //Method that displays a page where the customer can view the fare of the journey and rare the journey
    private void customerToDest(){
        //Hides the logout button
        mLogout.setVisibility(View.INVISIBLE);
        //Displays the page with the fare and the option to rate the journey
        arrivalinfo.setVisibility(View.VISIBLE);
        //Hides the search bar where customer can search a location
        search_bar.setVisibility(View.INVISIBLE);
        //Calculating the fare using this method
        getFare(dist, dur);
        //Displaying the fare
        fareInfo.setText("Total cost of journey: £ "+ String.valueOf(fare));

    }

    // converts a polyline list into a list of LatLng to allow for car animation
    private List<LatLng> getRouteCoords(){
        result.clear();
        // loops through all polylines, getting a list of LatLng points from each, combining all polyline points together in one list
        for(Polyline polly: polylines){
            List<LatLng> temp = polly.getPoints();
            for(LatLng latlng:temp){
                result.add(latlng);
            }
        }
        Collections.reverse(result);
        return result;
    }

    protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(MapsActivity.this).addConnectionCallbacks(MapsActivity.this).addOnConnectionFailedListener(MapsActivity.this).addApi(LocationServices.API).build();
        mGoogleApiClient.connect();                                                                                                                                                     //Allows us to use this api
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {                                                          //when the map is called and everything is ready to start working
        mLocationRequest  = new LocationRequest();                                                               //create a request to get lpocation from second to second
        mLocationRequest.setInterval(1000);                                                                      //Set seconds timer 1000 milliseconds
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);                                   //Accuracy obtained for map is optimum; drawback is drains a lot of battery

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {                                                             //if statement necessary to check if we have all the permissions
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);                                  //This statement enables to refresh within an interval of 1000 milliseconds(1sec) intervals
    }

    //Method to to get route to the destination(Taken from https://github.com/jd-alexander/Google-Directions-Android/)
    private void getRouteToMLocation(LatLng pickupLatLng){
        //Instantiating a Routing object
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                //Delivers routing results
                .withListener(this)
                //set to false, as there is only a requirement for one route to travel from customer's pickuplocation to destination
                .alternativeRoutes(false)
                //api key
                .key("AIzaSyAF2okyqOIjAjCtHmWaX97T7_3792RRwXo")
                .waypoints(new LatLng(newLocation.getLatitude(), newLocation.getLongitude()), pickupLatLng)
                .build();
        //Execute routing, which would generate override methods
        routing.execute();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    //Method for when the routing request failed
    @Override
    public void onRoutingFailure(RouteException e) {
        // The Routing request failed

        if(e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {

    }

    //Method to add route to map
    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        //Checking if polylines already exist on map, then remove them first
        if(polylines.size() > 0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();

        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            //polyoptions is used to describe how the polyline will look
            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            //draws the polyline on the map
            polylines.add(polyline);
            //Displays the distance and the duration to reach a the point store in route.get(i)
            disTime.setText(new StringBuilder().append("Distance: ").append(route.get(i).getDistanceText()).append(" ").append("Duration: ").append(route.get(i).getDurationText()));
            dist = route.get(i).getDistanceValue();
            dur = route.get(i).getDurationValue();

        }
        callBackAnimator();
    }

    //This method just checks if operation is not 0, then it would make the car marker move
    private void callBackAnimator() {

        if(operation != 0){
            getRouteCoords();
            Collections.reverse(result);
            CarAnimation(mMap, currentCarPos, result, false);
        }
    }

    @Override
    public void onRoutingCancelled(){

    }

    //Method to erase polylines
    private void erasePolylines(){
        for(Polyline line : polylines){
            line.remove();
        }
        polylines.clear();
    }
}
