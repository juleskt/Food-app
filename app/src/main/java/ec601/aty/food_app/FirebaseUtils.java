package ec601.aty.food_app;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

                        // Delete if expired
                        if (DateAndTimeUtils.checkIfUnixTimeIsExpired(pointToAdd.getExpiryUnixTime()))
                        {
                            autoDeletePointData(key, pointToAdd.getPosterID());
                        } else
                        {
                            displayMapPointOnMap(
                                    pointToAdd,
                                    createGeoFireKeyToProducerKeyPair(pointToAdd, key)
                            );
                        }
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
                        .title(mapPoint.getProducerName())
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
        } catch (Exception e)
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
        } catch (Exception e)
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
    }

    public static void updateMapPoint(String geofireKey, MapPoint pointToModify)
    {
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

        ref.child(geofireKey).addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            @SuppressWarnings("unchecked")
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                MapPoint pointAssociatedWithProducerFromDB = dataSnapshot.getValue(MapPoint.class);
                String currentConsumerName = UserUtils.currentUserSingleton.getName();

                if (pointAssociatedWithProducerFromDB != null)
                {
                    // Not first consumer
                    if (pointAssociatedWithProducerFromDB.getInterestedConsumers() != null)
                    {
                        // Consumer is not present
                        if (pointAssociatedWithProducerFromDB.getInterestedConsumers().get(currentConsumerName) == null)
                        {
                            Map<String, Object> consumerInfoMap = pointAssociatedWithProducerFromDB.getInterestedConsumers();
                            Map<String, Object> reservationMap = new HashMap<>();
                            reservationMap.put("reservationAmount", reservationAmount);
                            consumerInfoMap.put(currentConsumerName, reservationMap);
                        }
                        // If consumer is already present
                        else
                        {
                            Map<String, Object> consumerMap = pointAssociatedWithProducerFromDB.getInterestedConsumers();
                            Object reservationMap = consumerMap.get(currentConsumerName);
                            long currentReservationAmount = (long) ((HashMap) reservationMap).get("reservationAmount");
                            ((HashMap) reservationMap).put("reservationAmount", currentReservationAmount + reservationAmount);
                        }
                    }
                    // First consumer
                    else
                    {
                        Map<String, Object> consumerInfoMap = new HashMap<>();
                        Map<String, Object> reservationMap = new HashMap<>();
                        reservationMap.put("reservationAmount", reservationAmount);
                        consumerInfoMap.put(currentConsumerName, reservationMap);

                        pointAssociatedWithProducerFromDB.setInterestedConsumers(consumerInfoMap);
                    }
                }

                DatabaseReference pointRef = database
                        .getReference(POINT_DATA_NODE_PATH);

                Map<String, Object> pointObject = new HashMap<>();
                pointObject.put(geofireKey, pointAssociatedWithProducerFromDB);
                pointRef.updateChildren(pointObject);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    public static void getPointDataForProducerManagement(String geofireKey, Dialog dialog, Context context)
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database
                .getReference(POINT_DATA_NODE_PATH);

        ref.child(geofireKey).addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                MapPoint producerPoint = dataSnapshot.getValue(MapPoint.class);

                TextView food_quantity = dialog.findViewById(R.id.remaining_food);
                food_quantity.setText(String.valueOf(producerPoint.getQuantity())+" " + producerPoint.getUnit());

                TextView time_left = dialog.findViewById(R.id.remaining_time);
                float intermediate_time = producerPoint.getExpiryUnixTime() - DateAndTimeUtils.getCurrentUnixTime();

                // Converting Unix Time to hours; 3600000 is the factor to convert milliseconds to hours
                intermediate_time = intermediate_time / 3600000;
                time_left.setText(String.format("%.2f", intermediate_time));
                final long initial_time = (long) intermediate_time;

                // This button is used to update the food
                Button dialogButton = dialog.findViewById(R.id.add_food);
                dialogButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        String quantity_str = (((TextView) dialog.findViewById(R.id.add_food_quantity)).getText().toString());
                        long quantity;
                        try
                        {
                            quantity = Long.parseLong(quantity_str);
                            if (quantity + producerPoint.getQuantity() < 0)
                            {
                                throw new Exception();
                            }

                            producerPoint.setQuantity(producerPoint.getQuantity() + quantity);
                            FirebaseUtils.updateMapPoint(geofireKey, producerPoint);
                            dialog.dismiss();
                        } catch (Exception e)
                        {
                            Toast.makeText(context, "Please enter a valid input that doesn't reduce quantity to less than zero.", Toast.LENGTH_LONG).show();
                            return;
                        }
                    }
                });

                // This button is used to update the food
                dialogButton = dialog.findViewById(R.id.add_time);
                dialogButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        String quantity_str = (((TextView) dialog.findViewById(R.id.add_time_quantity)).getText().toString());
                        long quantity;
                        try
                        {
                            quantity = Long.parseLong(quantity_str);
                            if (quantity + initial_time < 0)
                            {
                                throw new Exception();
                            }
                            producerPoint.setExpiryUnixTime(DateAndTimeUtils.addHoursToUnixTime(producerPoint.getExpiryUnixTime(), (int) quantity));
                            FirebaseUtils.updateMapPoint(geofireKey, producerPoint);
                            dialog.dismiss();
                        } catch (Exception e)
                        {
                            Toast.makeText(context, "Please enter a valid input that doesn't reduce time to less than zero.", Toast.LENGTH_LONG).show();
                            return;
                        }
                    }
                });

                try
                {
                    Map<String, Object> interestedConsumers = producerPoint.getInterestedConsumers();
                } catch (NullPointerException e)
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

    public static void deletePointData(String pointDataKey)
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database
                .getReference(POINT_DATA_NODE_PATH)
                .child(pointDataKey);

        ref.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                dataSnapshot.getChildren().forEach(pointDataChild -> pointDataChild.getRef().removeValue());
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    public static void autoDeletePointData(String geofireKey, String producerKey)
    {
        GeoFireUtils.deleteGeofirePoint(geofireKey);
        UserUtils.findInterestedConsumersFromProducerKeyAndDelete(producerKey);
        FirebaseUtils.deletePointData(geofireKey);
        UserUtils.deleteInterestedConsumerAndLocationKeys(producerKey);
    }
}
