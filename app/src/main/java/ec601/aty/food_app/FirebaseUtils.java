package ec601.aty.food_app;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Ryouk on 10/17/2017.
 */

public class FirebaseUtils {
    private static final String POINT_DATA_NODE_PATH = "pointData";

    public static void pushPointData(String key, String value) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(POINT_DATA_NODE_PATH).child(key);
        myRef.setValue(value);
    }
}
