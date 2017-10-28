package ec601.aty.food_app;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapUtils {
    private static GoogleMap mMap;

    public static void setMap(GoogleMap gMap) {
        mMap = gMap;
    }

    public static void addMarkerToMap(MarkerOptions markerOption) {
        mMap.addMarker(markerOption);
    }
}
