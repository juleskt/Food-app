package ec601.aty.food_app;

import com.google.android.gms.maps.model.LatLng;

public class MapPoint
{
    private LatLng coordinates;
    private String description;
    private long createdUnixTime;
    private long expiryUnixTime;

    public MapPoint() {}

    public MapPoint(LatLng coordinates)
    {
        this.coordinates = coordinates;
    }

    public MapPoint(LatLng coordinates, String description)
    {
        this.description = description;
        this.coordinates = coordinates;
    }

    public MapPoint(LatLng coordinates, String description, long createdUnixTime, long expiryUnixTime)
    {
        this.description = description;
        this.createdUnixTime = createdUnixTime;
        this.expiryUnixTime = expiryUnixTime;
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

    public long getCreatedUnixTime()
    {
        return this.createdUnixTime;
    }

    public void setCreatedUnixTime(long createdUnixTime)
    {
        this.createdUnixTime = createdUnixTime;
    }

    public long getExpiryUnixTime()
    {
        return this.expiryUnixTime;
    }

    public void setExpiryUnixTime(long expiryUnixTime)
    {
        this.expiryUnixTime = expiryUnixTime;
    }
}
