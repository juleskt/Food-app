package ec601.aty.food_app;

import android.Manifest;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;

import android.support.v4.content.ContextCompat;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;

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
    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private MapPoint currentMapPoint = null;

    private final static int FINE_LOCATION_PERMISSION = 1;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9001;
    private static final String TAG = "MAPS_ACTIVITY";

    private TextView userEmail;
    private EditText radiusText;

    //Stuff for the navigation drawer
    private ListView mDrawerList;
    private ArrayAdapter<String> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mDrawerList = (ListView) findViewById(R.id.navigation_drawer_list);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        //Making sure Google maps is gucci
        if (checkPlayServices())
        {
            // Building the GoogleApi client
            buildGoogleApiClient();
        }

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener()
        {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth)
            {
                if (firebaseAuth.getCurrentUser() != null)
                {
                    userEmail = (TextView) findViewById(R.id.userEmail);
                    userEmail.setText(mAuth.getCurrentUser().getEmail());
                } else
                {
                    UserUtils.currentUserSingleton = null;
                }
            }
        };


        if (mAuth.getCurrentUser() == null || UserUtils.currentUserSingleton == null)
        {
            startActivity(new Intent(MapsActivity.this, LoginActivity.class));
        }
        else
        {
            UserUtils.getCurrentUserDetails(mAuth);
            if (UserUtils.isCurrentUserProducer())
            {
                Button locations = findViewById(R.id.findLocations);
                locations.setVisibility(View.GONE);

                EditText radius = findViewById(R.id.radiusText);
                radius.setVisibility(View.GONE);

                NotificationManager producerNotificationManager =
                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                UserUtils.setInterestedConsumerNotificationForProducer(producerNotificationManager, this, mAuth);
            }
            else
            {
                Button publish = findViewById(R.id.sendLocationToFireBase);
                publish.setVisibility(View.GONE);
            }
            addDrawerItems();
        }

        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                FINE_LOCATION_PERMISSION);

        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        setUpMapIfNeeded();
    }

    // Update the live location dot
    private void displayLocation()
    {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED)
        {
            LocationServices.getFusedLocationProviderClient(this).getLastLocation()
                    .addOnSuccessListener(this, (location) ->
                    {
                        if (location != null)
                        {
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
                            mMap.animateCamera(cameraUpdate);
                        }
                    });
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
        MapUtils.setMap(mMap);
        setUpMap();
        if (UserUtils.isCurrentUserConsumer())
        {
            MapUtils.setUpHandlerForMarkerClicks(MapsActivity.this, mAuth);
        }
        displayLocation();
    }

    @Override
    public void onMapClick(LatLng point)
    {
        if (UserUtils.isCurrentUserProducer())
        {
            if (!((ProducerUser) UserUtils.currentUserSingleton).checkIfProducerIsAtLimit())
            {
                currentMapPoint = new MapPoint(point.latitude, point.longitude);
                mMap.addMarker(new MarkerOptions()
                        .position(point)
                        .title("You are here")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            }
            else
            {
                Toast.makeText(
                        MapsActivity.this,
                        "You are currently at your point limit",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    public void onMapClearClick(View view)
    {
        currentMapPoint = null;
        MapUtils.clearMap();
    }

    public void onMapPublishClick(View view)
    {
        if (UserUtils.isCurrentUserProducer())
        {
            if (null == currentMapPoint)
            {
                Toast.makeText(
                        getApplicationContext(),
                        "Please place a marker",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            final Dialog dialog = new Dialog(MapsActivity.this);
            dialog.setContentView(R.layout.publish_dialog);
            dialog.setTitle("Food publish details");

            Spinner unit_spinner = (Spinner) dialog.findViewById(R.id.unit_selection);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.unit_types, android.R.layout.simple_spinner_item);

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            unit_spinner.setAdapter(adapter);
            Button dialogButton = (Button) dialog.findViewById(R.id.publish_dialog_button);
            // if button is clicked, close the custom dialog
            dialogButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    FirebaseUtils.produceDialogPublish(MapsActivity.this, dialog, currentMapPoint);
                }
            });
            dialog.show();
        }
    }

    public void onMapFindLocationsClick(View view)
    {
        // radiusText is what you can use for the query.
        radiusText = findViewById(R.id.radiusText);

        if (((radiusText.getText().toString()).equals("")))
        {
            GeoFireUtils.setGeoQueryLocation(mMap.getCameraPosition().target);
            GeoFireUtils.radiusGeoQuery(mMap);
        } else if (Double.parseDouble(radiusText.getText().toString()) > 20.0)
        {
            Toast.makeText(
                    getApplicationContext(),
                    "The maximum query distance is 20 km",
                    Toast.LENGTH_SHORT).show();

            GeoFireUtils.setGeoQueryLocation(mMap.getCameraPosition().target, 20);
            GeoFireUtils.radiusGeoQuery(mMap);
        } else
        {
            GeoFireUtils.setGeoQueryLocation(mMap.getCameraPosition().target, Double.parseDouble(radiusText.getText().toString()));
            GeoFireUtils.radiusGeoQuery(mMap);
        }
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
            //  mMap.setBuildingsEnabled(false);
            // Turn off basic menu
            mMap.getUiSettings().setMapToolbarEnabled(false);
            // Set up map click listeners
            mMap.setOnMapClickListener(this);
            mMap.setOnMapLongClickListener(this);
            // Set up map movement listeners
            mMap.setOnCameraIdleListener(this);
            mMap.setOnCameraMoveStartedListener(this);
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
//        Toast.makeText(
//                getApplicationContext(),
//                "Connecting to Google API...",
//                Toast.LENGTH_SHORT).show();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case FINE_LOCATION_PERMISSION:
            {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    // @TODO Some checking
                } else
                {
                    // @TODO permission denied, boo! Disable the functionality that depends on this permission.
                }
                return;
            }
            // @TODO other 'case' lines to check for other permissions this app might request
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
    }

    //When connected to API
    @Override
    public void onConnected(Bundle bundle)
    {
        // displayLocation();
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

    //start authentication methods
    protected void onStart()
    {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    private void addDrawerItems()
    {
        final int USER_PROFILE_NAVIGATION_ITEM = 0;
        final int MANAGE_FOOD_NAVIGATION_ITEM = 1;
        final int LOGOUT_NAVIGATION_ITEM = 2;

        String[] osArray = {mAuth.getCurrentUser().getEmail(), "Manage Food", getString(R.string.logout)};
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, osArray);
        mDrawerList.setAdapter(mAdapter);
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                // Refresh current user data after adding point
                UserUtils.searchForForUserTypeData(mAuth, UserUtils.currentUserSingleton);

                // Refresh current user data after adding point
                UserUtils.searchForForUserTypeData(mAuth, UserUtils.currentUserSingleton);

                // Todo: Determine a better way to do this instead of hardcoded method
                if (position == LOGOUT_NAVIGATION_ITEM)
                {
                    UserUtils.safeSignOut(mAuth);
                    Toast.makeText(MapsActivity.this, "Signing Out", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(MapsActivity.this, LoginActivity.class));
                }
                else if (position == MANAGE_FOOD_NAVIGATION_ITEM)
                {
                    if (UserUtils.isCurrentUserProducer())
                    {
                        UserUtils.getPointDataForProducerManage();
                        UserUtils.deletePointDataFromManagement(mAuth);

                    }
                    else if (UserUtils.isCurrentUserConsumer())
                    {
                        UserUtils.getProducerDataForConsumerManage();
                    }
                }
                else
                {
                    Toast.makeText(MapsActivity.this, String.valueOf(position), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
