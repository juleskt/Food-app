package ec601.aty.food_app;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Created by Dennis on 12/9/2017.
 */

public class testGeoFireData {
    public static final String GEOFIRE_NODE_PATH = "geoFireDataTest";

    private static void testPoint(double lat, double lng, LatLng coordinates){

        if (lat == coordinates.latitude && lng == coordinates.longitude)
            System.out.println("Coordinates match");
        else
            System.out.println("Coordinates don't match");
    };

    private static void checkProducerPoint(String key, LatLng coordinates){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(GEOFIRE_NODE_PATH);
        Query geofireQuery = ref.child(key);

        geofireQuery.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                testGeoFireData.testPoint((double)dataSnapshot.child("0").getValue(),(double)dataSnapshot.child("1").getValue(), coordinates);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }
}
