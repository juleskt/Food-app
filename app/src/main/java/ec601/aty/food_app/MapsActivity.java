package ec601.aty.food_app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;

import android.location.Location;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.maps.model.MarkerOptions;


import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnCameraIdleListener,
        GoogleMap.OnCameraMoveStartedListener
{

    public static GoogleMap mMap;
    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private SimplePoint markerLocation;
    private final static int FINE_LOCATION_PERMISSION = 1;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9001;
    public static Map<String, LatLng> geofireKeysLatLngMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        //Making sure Google maps is gucci
        if (checkPlayServices())
        {
            // Building the GoogleApi client
            buildGoogleApiClient();
        }

        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                FINE_LOCATION_PERMISSION);

        mapFragment.getMapAsync(this);
        setUpMapIfNeeded();
        displayLocation();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        setUpMapIfNeeded();
    }

    //Update the live location dot
    private void displayLocation()
    {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED)
        {
            mLastLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);

            if (mLastLocation != null)
            {
                double latitude = mLastLocation.getLatitude();
                double longitude = mLastLocation.getLongitude();
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 14.0f));
            } else
            {

            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
        setUpMap();
    }

    @Override
    public void onMapClick(LatLng point)
    {

        // Write a message to the database
        // FirebaseDatabase database = FirebaseDatabase.getInstance();
        // DatabaseReference myRef = database.getReference("testCoordinates").push();
        // String firebasePushKey = myRef.push().getKey();

        markerLocation = new SimplePoint(point.latitude, point.longitude);
        mMap.addMarker(new MarkerOptions()
                .position(point)
                .title("You are here")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
    }


    public void onMapLoginClick(View view)
    {
        Toast.makeText(
                getApplicationContext(),
                "Login Functionality not yet implemented",
                Toast.LENGTH_SHORT).show();
    }

    public void onMapClearClick(View view)
    {
        mMap.clear();
    }

    public void onMapPublishClick(View view)
    {
        String refKey = GeoFireUtils.pushLocationToGeofire(markerLocation.getLatLng());
    }

    @Override
    public void onMapLongClick(LatLng point)
    {
        mMap.clear();
    }

    private void setUpMap()
    {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED)
        {
            // Live locations
            mMap.setMyLocationEnabled(true);
            // Remove buildings
            mMap.setBuildingsEnabled(false);
            // Turn off basic menu
            mMap.getUiSettings().setMapToolbarEnabled(false);
            // Set up map click listeners
            mMap.setOnMapClickListener(this);
            mMap.setOnMapLongClickListener(this);
            // Set up map movement listeners
            mMap.setOnCameraIdleListener(this);
            mMap.setOnCameraMoveStartedListener(this);
            displayLocation();
        }
    }

    //Set up the map the first time
    private void setUpMapIfNeeded()
    {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null)
        {
            // Try to obtain the map from the SupportMapFragment.
            ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMapAsync(this);
        }
    }

    //Checking if we are connected
    private boolean checkPlayServices()
    {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS)
        {
            if (googleAPI.isUserResolvableError(result))
            {
                googleAPI.getErrorDialog(
                        this,
                        result,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            return false;
        }
        return true;
    }

    //Connecting to actual client
    protected synchronized void buildGoogleApiClient()
    {
        Toast.makeText(
                getApplicationContext(),
                "Connecting to Google API...",
                Toast.LENGTH_SHORT).show();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case FINE_LOCATION_PERMISSION:
            {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {

                } else
                {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onCameraMoveStarted(int reason)
    {
    }

    @Override
    public void onCameraIdle()
    {
        GeoFireUtils.setGeoQueryLocation(mMap.getCameraPosition().target);
        GeoFireUtils.radiusGeoQuery(mMap);
    }

    //When connected to API
    @Override
    public void onConnected(Bundle bundle)
    {
        displayLocation();
    }

    // Spam retry, lol maybe want to have better behavior in the future
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult)
    {
    }

    //When disconnected, try to recon
    @Override
    public void onConnectionSuspended(int i)
    {
        mGoogleApiClient.connect();
    }
}
