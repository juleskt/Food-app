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

import java.util.HashMap;
import java.util.Map;

public class GeoFireUtils {
    private static final String GEOFIRE_NODE_PATH = "geoFireAyy";
    private static GeoQuery geoQuery;
    private static Map<String, LatLng> geofireKeysLatLngMap = new HashMap<>();

    public static String pushLocationToGeofire(LatLng latLng) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(GEOFIRE_NODE_PATH);
        String firebasePushKey = FirebaseDatabase.getInstance().getReference(GEOFIRE_NODE_PATH).push().getKey();
        GeoFire geoFireRef = new GeoFire(ref);
        geoFireRef.setLocation(firebasePushKey, new GeoLocation(latLng.latitude, latLng.longitude), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                if (error == null) {

                } else {
                    System.err.println("There was an error saving the location to GeoFire: " + error);

                }
            }
        });

        return firebasePushKey;
    }

    public static void setGeoQueryLocation(LatLng centerLatLng) {
        setGeoQueryLocation(centerLatLng, 1);
    }

    public static void setGeoQueryLocation(LatLng centerLatLng, double queryRadius) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(GEOFIRE_NODE_PATH);
        GeoFire geoFire = new GeoFire(ref);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(centerLatLng.latitude, centerLatLng.longitude), queryRadius);
    }

    public static void radiusGeoQuery(GoogleMap mMap) {
        geofireKeysLatLngMap.clear();
        mMap.clear();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                geofireKeysLatLngMap.put(key, new LatLng(location.latitude, location.longitude));
            }

            @Override
            public void onKeyExited(String key) {}

            @Override
            public void onKeyMoved(String key, GeoLocation location) {}

            @Override
            public void onGeoQueryReady() {
                geoQuery.removeAllListeners();
                displayGeoQueryResultsOnMap(mMap);
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {}
        });
    }

    private static void displayGeoQueryResultsOnMap(GoogleMap mMap) {
        // Do some relation with Firebase objects to get actual data
        geofireKeysLatLngMap.forEach( (key, value) -> {
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(value.latitude, value.longitude))
                    .title("test")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        });
    }
}
