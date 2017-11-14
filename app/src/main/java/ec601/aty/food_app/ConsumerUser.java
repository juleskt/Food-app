package ec601.aty.food_app;

public class ConsumerUser extends User {

    public ConsumerUser(AccountType accountType, String name) {
        super(accountType, name);
    }

    public ConsumerUser( String name) {
       this.name = name;
    }

    public ConsumerUser(User user) {
        this.setAccountType(user.getAccountType());
        this.setName(user.getName());
    }

    public ConsumerUser() {}
}
