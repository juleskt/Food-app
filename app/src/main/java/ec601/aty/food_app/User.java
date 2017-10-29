package ec601.aty.food_app;

public class User {
    public enum AccountType {
        PRODUCER,
        CONSUMER
    }

    private String name;
    private AccountType accountType;

    public User(){

    }

    public User(AccountType accountType, String name){
        this.name = name;
        this.accountType = accountType;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
