package ec601.aty.food_app;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapUtils
{
    private static GoogleMap mMap;
    private static Map<String, Map<String, String>> markerMap = new HashMap<>();

    public static void setMap(GoogleMap gMap)
    {
        mMap = gMap;
    }

    public static void addMarkerToMap(MarkerOptions markerOption, Map<String, String> idMap)
    {
        Marker newMarker = mMap.addMarker(markerOption);
        markerMap.put(newMarker.getId(), idMap);
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
    }

    public static void setUpHandlerForMarkerClicks()
    {
        mMap.setOnInfoWindowClickListener(clickedMarker ->
        {
            // Handle what happens when a user clicks the info bubble of a marker. Below is how we extract the keys from the marker
            Map<String, String> geoFireKeyToProducerKeyPair = markerMap.get(clickedMarker.getId());
            geoFireKeyToProducerKeyPair.forEach((geoFireKey, producerKey) ->
            {

            });
        });
    }
}
