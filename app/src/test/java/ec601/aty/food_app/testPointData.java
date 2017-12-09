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
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Created by Dennis on 12/9/2017.
 */

public class testPointData {
    public static final String POINT_DATA_NODE_PATH = "pointDataTest";

    private static void testPoint(MapPoint pointDB, MapPoint pointTest){
        if (pointDB.getCoordinates().equals(pointTest.getCoordinates()))
            System.out.println("Coordinates match");
        else
            System.out.println("Coordinates don't match");
        if (pointDB.getCreatedUnixTime() == pointDB.getCreatedUnixTime())
            System.out.println("Created Unix Time match");
        else
            System.out.println("Created Unix Time doesn't match");
        if (pointDB.getExpiryUnixTime()==pointTest.getExpiryUnixTime())
            System.out.println("Expiry Unix Time match");
        else
            System.out.println("Expiry Unix Time doesn't match");
        if (pointDB.getPosterID().equals(pointTest.getPosterID()))
            System.out.println("Poster ID match");
        else
            System.out.println("Poster ID don't match");
        if (pointDB.getUnit().equals(pointTest.getUnit()))
            System.out.println("Units match");
        else
            System.out.println("Units don't match");
        if (pointDB.getQuantity() == pointTest.getQuantity())
            System.out.println("Quantities match");
        else
            System.out.println("Quantities don't match");
        if (pointDB.getProducerName().equals(pointTest.getProducerName()))
            System.out.println("Producer Name match");
        else
            System.out.println("Producer Name doesn't match");
    };

    private static void checkProducerPoint(String key, MapPoint value){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference(POINT_DATA_NODE_PATH);

        ref.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                MapPoint pointToAdd = dataSnapshot.getValue(MapPoint.class);
                testPointData.testPoint(pointToAdd, value);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}
