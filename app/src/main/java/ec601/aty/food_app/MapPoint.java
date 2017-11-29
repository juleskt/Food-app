package ec601.aty.food_app;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.Exclude;

import java.util.Map;

public class MapPoint
{
    private double latitude;
    private double longitude;
    private String description;
    private long createdUnixTime;
    private long expiryUnixTime;
    private String posterID;
    private long quantity;
    private String unit;
    private String producerName;

    @Exclude
    private Map<String, String> keyProducerPair;

    public MapPoint()
    {
    }

    public MapPoint(double latitude, double longitude)
    {
        this.latitude = latitude;
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

    @Exclude
    public LatLng getCoordinates()
    {
        return new LatLng(latitude, longitude);
    }

    @Exclude
    public void setCoordinates(LatLng coordinates)
    {
        this.latitude = coordinates.latitude;
        this.longitude = coordinates.longitude;
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

    public String getPosterID()
    {
        return posterID;
    }

    public void setPosterID(String posterID)
    {
        this.posterID = posterID;
    }

    public long getQuantity()
    {
        return quantity;
    }

    public String getUnit()
    {
        return unit;
    }

    public void setQuantity(long inQuantity)
    {
        this.quantity = inQuantity;
    }

    public void setUnit(String inUnit)
    {
        this.unit = inUnit;
    }

    @Exclude
    public Map<String, String> getKeyProducerPair()
    {
        return keyProducerPair;
    }

    @Exclude
    public void setKeyProducerPair(Map<String, String> keyProducerPair)
    {
        this.keyProducerPair = keyProducerPair;
    }


    public String getProducerName()
    {
        return producerName;
    }

    public void setProducerName(String producerName)
    {
        this.producerName = producerName;
    }

}
