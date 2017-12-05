package ec601.aty.food_app;

import com.google.firebase.database.Exclude;

import java.util.Map;

public class ProducerUser extends User
{
    private int PRODUCER_POINT_LIMIT = 1;
    Map<String, Object> locationKeys;
    Map<String, Object> interestedConsumers;

    public ProducerUser(AccountType accountType, String name)
    {
        super(accountType, name);
    }

    public ProducerUser(String name)
    {
        this.name = name;
    }

    public ProducerUser(User user)
    {
        this.setAccountType(user.getAccountType());
        this.setName(user.getName());
    }

    public ProducerUser()
    {
    }

    public Map<String, Object> getLocationKeys()
    {
        return locationKeys;
    }

    public void setLocationKeys(Map<String, Object> locationKeys)
    {
        this.locationKeys = locationKeys;
    }

    public boolean isProducerBelowPointLimit()
    {
        return locationKeys == null || locationKeys.size() < PRODUCER_POINT_LIMIT;

    }

    @Exclude
    public boolean addLocation(String geofireKey, MapPoint point)
    {
        if (isProducerBelowPointLimit())
        {
            locationKeys.put(geofireKey, point);
            return true;
        }

        return false;
    }

    public Map<String, Object> getInterestedConsumers()
    {
        return interestedConsumers;
    }

    public void setInterestedConsumers(Map<String, Object> interestedConsumers)
    {
        this.interestedConsumers = interestedConsumers;
    }
}
