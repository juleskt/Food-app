package ec601.aty.food_app;

import java.util.List;
import java.util.Map;

public class ProducerUser extends User
{

    Map<String, Object> locationKeys;

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
}
