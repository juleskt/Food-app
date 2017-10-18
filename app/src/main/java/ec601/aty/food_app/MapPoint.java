package ec601.aty.food_app;

import com.google.android.gms.maps.model.LatLng;

import java.util.GregorianCalendar;

public class MapPoint
{
    private LatLng coordinates;
    private String description;
    private GregorianCalendar createdTime;
    private GregorianCalendar expiryTime;

    public MapPoint(LatLng coordinates)
    {
        this.coordinates = coordinates;
    }

    public MapPoint(LatLng coordinates, String description)
    {
        this.description = description;
        this.coordinates = coordinates;
    }

    public MapPoint(LatLng coordinates, String description, GregorianCalendar createdTime, GregorianCalendar expiryTime)
    {
        this.description = description;
        this.createdTime = createdTime;
        this.expiryTime = expiryTime;
        this.coordinates = coordinates;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public LatLng getCoordinates()
    {
        return coordinates;
    }

    public void setCoordinates(LatLng coordinates)
    {
        this.coordinates = coordinates;
    }

    public GregorianCalendar getCreatedTime()
    {
        return this.createdTime;
    }

    public void setCreatedTime(GregorianCalendar createdTime)
    {
        this.createdTime = createdTime;
    }

    public GregorianCalendar getExpiryTime()
    {
        return this.expiryTime;
    }

    public void setExpiryTime(GregorianCalendar expiryTime)
    {
        this.expiryTime = expiryTime;
    }
}
