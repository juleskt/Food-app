package ec601.aty.food_app;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapUtils
{
    private static GoogleMap mMap;
    // Maps Google Marker IDs to geofireKey <-> MapPoint
    private static Map<String, Map<String, MapPoint>> markerMap = new HashMap<>();

    public static void setMap(GoogleMap gMap)
    {
        mMap = gMap;
    }

    public static void addMarkerToMap(MarkerOptions markerOption, Map<String, MapPoint> geofireKeyToPointMap)
    {
        Marker newMarker = mMap.addMarker(markerOption);
        markerMap.put(newMarker.getId(), geofireKeyToPointMap);
    }

    public static void addMarkersToMap(List<MarkerOptions> markerOptions)
    {
        markerOptions.forEach(markerOption ->
                mMap.addMarker(markerOption)
        );
    }

    public static void clearMap()
    {
        mMap.clear();
        markerMap.clear();
    }

    public static void setUpHandlerForMarkerClicks(Context maps_activity, FirebaseAuth mAuth)
    {
        mMap.setOnInfoWindowClickListener(clickedMarker ->
        {
            // Handle what happens when a user clicks the info bubble of a marker. Below is how we extract the keys from the marker
            Map<String, MapPoint> geoFireKeyToMapPointPair = markerMap.get(clickedMarker.getId());

            final Dialog consumer_dialog = new Dialog(maps_activity);
            consumer_dialog.setContentView(R.layout.consume_dialog);
            consumer_dialog.setTitle("Food Consumption details");

            geoFireKeyToMapPointPair.forEach((geoFireKey, mapPoint) ->
            {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference ref = database.getReference(FirebaseUtils.POINT_DATA_NODE_PATH);

            });
            Button consumerDialogButton = (Button) consumer_dialog.findViewById(R.id.reserve_food);
            // if button is clicked, close the custom dialog
            consumerDialogButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    geoFireKeyToMapPointPair.forEach((geoFireKey, mapPoint) ->
                    {
                        long reservationAmount = FirebaseUtils.consumeDialogPublish(maps_activity, consumer_dialog, geoFireKey, mapPoint);

                        if (reservationAmount > 0)
                        {
                            UserUtils.registerProducerAsInterestForConsumerFromPoint(geoFireKey, mapPoint, mAuth, reservationAmount);
                            FirebaseUtils.registerInterestedConsumerUnderPoint(geoFireKey, mAuth, reservationAmount);
                        }
                    });
                }
            });
            consumer_dialog.show();
        });
    }

    public static void createPublishManageDialog(Context maps_activity, FirebaseAuth mAuth)
    {
        final Dialog dialog = new Dialog(maps_activity);
        dialog.setContentView(R.layout.producer_manage);
        dialog.setTitle("Point Management");

        UserUtils.getPointDataForProducerManage(dialog, maps_activity);


        // This button is used to close the dialog
        Button dialogButton = dialog.findViewById(R.id.exit_point_manage);
        dialogButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();
            }
        });

        // This button is used to remove a point
        dialogButton = dialog.findViewById(R.id.remove_point);
        dialogButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                UserUtils.deletePointDataFromManagement(mAuth);
                dialog.dismiss();
            }
        });
        dialog.show();
    }


    public static void createConsumerManageDialog(Context maps_activity, FirebaseAuth mAuth)
    {

        UserUtils.getProducerDataForConsumerManage();
    }
}
