package ec601.aty.food_app;

import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FirebaseUtils {
    private static final String POINT_DATA_NODE_PATH = "pointData";

    public static void pushPointData(String key, MapPoint value) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference(POINT_DATA_NODE_PATH).child(key);
        ref.setValue(value);
    }

    public static List<MapPoint> getMapPointsFromKeys(List<String> keys) {
        List<MapPoint> mapPoints = new ArrayList<>();
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference ref = database.child(POINT_DATA_NODE_PATH);

        Log.i("Why must you hate us?", "?????");

        keys.forEach(key -> {
            Query mapPointsFromGeoPoints = ref.equalTo(key);

            mapPointsFromGeoPoints.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapShotMapPoint : dataSnapshot.getChildren()) {
                        Log.i("Heyoo", "Huh?");
                        mapPoints.add(snapShotMapPoint.getValue(MapPoint.class));
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        });

        return mapPoints;
    }
}
