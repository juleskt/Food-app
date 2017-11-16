package ec601.aty.food_app;

import java.util.Map;

public class ConsumerUser extends User
{
    Map<String, Long> interestedInProducerList;

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

    public Map<String, Long> getInterestedInProducerList()
    {
        return interestedInProducerList;
    }

    public void setInterestedInProducerList(Map<String, Long> interestedInProducerList)
    {
        this.interestedInProducerList = interestedInProducerList;
    }
}
