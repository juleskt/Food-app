package ec601.aty.food_app;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class MapUtils
{
    private static GoogleMap mMap;

    public static void setMap(GoogleMap gMap)
    {
        mMap = gMap;
    }

    public static void addMarkerToMap(MarkerOptions markerOption)
    {
        mMap.addMarker(markerOption);
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
}
