package ec601.aty.food_app;

import com.google.android.gms.maps.model.LatLng;

public class SimplePoint
{
    private double latitude;
    private double longitude;
    private LatLng latLng;
    private String description;

    public SimplePoint(double latitude, double longitude)
    {
        this.latitude = latitude;
        this.longitude = longitude;
        this.latLng = new LatLng(this.latitude, this.longitude);
    }

    public SimplePoint(double latitude, double longitude, String description)
    {
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
        this.latLng = new LatLng(this.latitude, this.longitude);
    }

    public SimplePoint()
    {
    }

    public double getLatitude()
    {
        return latitude;
    }

    public void setLatitude(double latitude)
    {
        this.latitude = latitude;
    }

    public double getLongitude()
    {
        return longitude;
    }

    public void setLongitude(double longitude)
    {
        this.longitude = longitude;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public LatLng getLatLng()
    {
        return latLng;
    }

    public void setLatLng(LatLng latLng)
    {
        this.latLng = latLng;
    }
}
