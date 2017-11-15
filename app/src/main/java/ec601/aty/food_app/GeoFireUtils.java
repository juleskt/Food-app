package ec601.aty.food_app;

import android.util.Log;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GeoFireUtils
{
    private static final String GEOFIRE_NODE_PATH = "geoFireAyy";
    private static GeoQuery geoQuery;
    private static List<String> geofireKeysList = new ArrayList<>();

    public static String pushLocationToGeofire(LatLng latLng)
    {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(GEOFIRE_NODE_PATH);
        String firebasePushKey = FirebaseDatabase.getInstance().getReference(GEOFIRE_NODE_PATH).push().getKey();
        GeoFire geoFireRef = new GeoFire(ref);
        geoFireRef.setLocation(firebasePushKey, new GeoLocation(latLng.latitude, latLng.longitude), (key, error) ->
        {
            if (error != null)
            {
                // @TODO Handle Exception
                System.err.println("There was an error saving the location to GeoFire: " + error);
            }
        });

        return firebasePushKey;
    }

    public static void setGeoQueryLocation(LatLng centerLatLng)
    {
        setGeoQueryLocation(centerLatLng, 2);
    }

    public static void setGeoQueryLocation(LatLng centerLatLng, double queryRadius)
    {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(GEOFIRE_NODE_PATH);
        GeoFire geoFire = new GeoFire(ref);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(centerLatLng.latitude, centerLatLng.longitude), queryRadius);
    }

    public static void radiusGeoQuery(GoogleMap mMap)
    {
        geofireKeysList.clear();
        MapUtils.clearMap();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener()
        {
            @Override
            public void onKeyEntered(String key, GeoLocation location)
            {
                geofireKeysList.add(key);
            }

            @Override
            public void onKeyExited(String key)
            {
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location)
            {
            }

            @Override
            public void onGeoQueryReady()
            {
                geoQuery.removeAllListeners();
                displayGeoQueryResultsOnMap(geofireKeysList);
            }

            @Override
            public void onGeoQueryError(DatabaseError error)
            {
            }
        });
    }

    private static void displayGeoQueryResultsOnMap(List<String> geofireKeysList)
    {
        FirebaseUtils.populateMapWithMapPointsFromGeofireKeys(geofireKeysList);
    }
}
