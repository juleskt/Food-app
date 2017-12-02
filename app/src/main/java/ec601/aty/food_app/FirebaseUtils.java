package ec601.aty.food_app;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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

public class FirebaseUtils
{
    public static final String POINT_DATA_NODE_PATH = "pointData";

    public static void pushPointData(String key, MapPoint value)
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference(POINT_DATA_NODE_PATH).child(key);
        ref.setValue(value);
    }

    public static void populateMapWithMapPointsFromGeofireKeys(List<String> keys)
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference(POINT_DATA_NODE_PATH);

        keys.forEach(key ->
                ref.child(key).addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        MapPoint pointToAdd = dataSnapshot.getValue(MapPoint.class);

                        displayMapPointOnMap(
                                pointToAdd,
                                createGeoFireKeyToProducerKeyPair(pointToAdd, key)
                        );
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {
                    }
                })
        );
    }

    private static void displayMapPointOnMap(MapPoint mapPoint, Map<String, MapPoint> geofireKeyToPointMap)
    {
        MapUtils.addMarkerToMap(
            new MarkerOptions()
                    .position(mapPoint.getCoordinates())
                    .title("Expires at " + DateAndTimeUtils.getLocalFormattedDateFromUnixTime(mapPoint.getExpiryUnixTime()))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)),
            geofireKeyToPointMap
        );
    }

    // We need this mapping to identify which marker is clicked by a Consumer
    private static Map<String, MapPoint> createGeoFireKeyToProducerKeyPair(MapPoint point, String key)
    {
        Map<String, MapPoint> geofireKeyToPointMap = new HashMap<>();
        geofireKeyToPointMap.put(key, point);
        return geofireKeyToPointMap;
    }

    public static long consumeDialogPublish(Context context, Dialog dialog, String geofireKey, MapPoint mapPoint)
    {
        String quantity_str = (((TextView) dialog.findViewById(R.id.consume_quantity_box)).getText().toString());

        long quantity;
        try
        {
            quantity = Long.parseLong(quantity_str);
            if (quantity <= 0 || quantity > mapPoint.getQuantity())
            {
               throw new Exception();
            }

            mapPoint.setQuantity(mapPoint.getQuantity() - quantity);
            FirebaseUtils.updateMapPoint(geofireKey, mapPoint);
        }
        catch (Exception e)
        {
            Toast.makeText(context, "Please enter a valid input", Toast.LENGTH_LONG).show();
            return 0;
        }

        Toast.makeText(context, "" + quantity_str, Toast.LENGTH_LONG).show();
        dialog.dismiss();

        return quantity;
    }

    public static void produceDialogPublish(Context context, Dialog dialog, MapPoint currentMapPoint)
    {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String validity = (((TextView) dialog.findViewById(R.id.hours_available)).getText().toString());
        String quantity_str = (((TextView) dialog.findViewById(R.id.quantity_text_box)).getText().toString());
        if (validity.length() == 0)
        {
            // TODO: Maybe set error on the edittext instead of a toast?
            Toast.makeText(context, "Please enter how many hours your food will be available.", Toast.LENGTH_LONG).show();
            return;
        }
        if (quantity_str.length() == 0)
        {
            Toast.makeText(context, "Please enter how much food you will have available", Toast.LENGTH_LONG).show();
            return;
        }

        int hours;
        long quantity;
        try
        {
            hours = Integer.parseInt(validity);
            quantity = Integer.parseInt(quantity_str);
        }
        catch (Exception e)
        {
            Toast.makeText(context, "Please enter valid integer inputs", Toast.LENGTH_LONG).show();
            return;
        }
        String unit = (((Spinner) dialog.findViewById(R.id.unit_selection)).getSelectedItem().toString());

        long currentCreatedTime = DateAndTimeUtils.getCurrentUnixTime();
        currentMapPoint.setCreatedUnixTime(currentCreatedTime);
        currentMapPoint.setExpiryUnixTime(DateAndTimeUtils.addHoursToUnixTime(currentCreatedTime, hours));
        currentMapPoint.setPosterID(mAuth.getCurrentUser().getUid());
        currentMapPoint.setUnit(unit);
        currentMapPoint.setQuantity(quantity);
        currentMapPoint.setProducerName(UserUtils.currentUserSingleton.getName());

        String refKey = GeoFireUtils.pushLocationToGeofire(currentMapPoint.getCoordinates());
        UserUtils.addPointForCurrentProducer(refKey, mAuth);
        FirebaseUtils.pushPointData(refKey, currentMapPoint);

        dialog.dismiss();

        Toast.makeText(
                context,
                "Published point!",
                Toast.LENGTH_SHORT).show();
    }

    public static void updateMapPoint(String geofireKey, MapPoint pointToModify) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database
                .getReference(POINT_DATA_NODE_PATH);

        Map<String, Object> updatedMapPoint = new HashMap<>();
        updatedMapPoint.put(geofireKey, pointToModify);
        ref.updateChildren(updatedMapPoint);
    }

    public static void registerInterestedConsumerUnderPoint(String geofireKey, FirebaseAuth mAuth, long reservationAmount)
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database
                .getReference(POINT_DATA_NODE_PATH);

        ref.child(geofireKey).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                MapPoint pointAssociatedWithProducer = dataSnapshot.getValue(MapPoint.class);

                Map<String, Object> consumerInfoMap = new HashMap<>();
                Map<String, Object> reservationMap = new HashMap<>();
                reservationMap.put("reservationAmount", reservationAmount);
                consumerInfoMap.put(UserUtils.currentUserSingleton.getName(), reservationMap);

                pointAssociatedWithProducer.setInterestedConsumers(consumerInfoMap);

                DatabaseReference pointRef = database
                        .getReference(POINT_DATA_NODE_PATH);

                Map<String, Object> pointObject = new HashMap<>();
                pointObject.put(geofireKey, pointAssociatedWithProducer);
                pointRef.updateChildren(pointObject);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    public static void getPointDataForProducerManagement(String geofireKey)
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database
                .getReference(POINT_DATA_NODE_PATH);

        ref.child(geofireKey).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                MapPoint producerPoint = dataSnapshot.getValue(MapPoint.class);

                // @TODO ANISH: Point should have every thing you need for management

                try
                {
                    Map<String, Object> interestedConsumers = producerPoint.getInterestedConsumers();
                }
                catch (NullPointerException e)
                {
                    // No interested consumers :(
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }
}
