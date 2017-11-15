package ec601.aty.food_app;

import java.util.List;

public class ProducerUser extends User
{

    List<String> locationKeys;

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

    public List<String> getLocationKeys()
    {
        return locationKeys;
    }

    public void setLocationKeys(List<String> locationKeys)
    {
        this.locationKeys = locationKeys;
    }
}
