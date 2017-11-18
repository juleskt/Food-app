package ec601.aty.food_app;

import com.google.firebase.database.Exclude;

import java.util.Map;

public class ConsumerUser extends User
{
    Map<String, Object> interestedPointKeys;

    public ConsumerUser(AccountType accountType, String name)
    {
        super(accountType, name);
    }

    public ConsumerUser(String name)
    {
        this.name = name;
    }

    public ConsumerUser(User user)
    {
        this.setAccountType(user.getAccountType());
        this.setName(user.getName());
    }

    public ConsumerUser()
    {
    }

    public Map<String, Object> getInterestedPointKeys()
    {
        return interestedPointKeys;
    }

    public void setInterestedPointKeys(Map<String, Object> interestedPointKeys)
    {
        this.interestedPointKeys = interestedPointKeys;
    }
}
